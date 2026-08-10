package com.abrar.BOOKSTORE.login.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.jwt.JwtTokenProvider;
import com.abrar.BOOKSTORE.Login.user.UserPrincipal;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ContextConfiguration(classes = { JwtTokenProvider.class })
@ExtendWith(SpringExtension.class)
// A real base64-encoded 64-byte key - HS512 requires at least 512 bits, and
// JJWT 0.11.x's signWith(SignatureAlgorithm, String) overload treats the
// string as base64-encoded, so it has to actually decode to that length.
@TestPropertySource(properties = {
        "app.jwtSecret=fIj4ADMuns9PFaZZcbz0qwtUwnMHazUGjoVNlhpQHIMG2zIy+EYrEddOgy2ZQuVASAANAUW+mY05Mf50n/RLYA==",
        "app.jwtExpirationInMs=3600000" })
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    private Authentication authenticationFor(String username) {
        UserDetails principal = new UserPrincipal(1L, username, null, null, "hash",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void testGenerateTokenAndReadUsernameBack() {
        String token = jwtTokenProvider.generateToken(authenticationFor("reader"));

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertEquals("reader", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void testGenerateTokenFromUsernameDirectly() {
        String token = jwtTokenProvider.generateToken("librarian");
        assertEquals("librarian", jwtTokenProvider.getUsernameFromToken(token));
    }

    @Test
    void testValidateTokenAcceptsARealToken() {
        String token = jwtTokenProvider.generateToken(authenticationFor("reader"));
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void testValidateTokenRejectsGarbage() {
        assertFalse(jwtTokenProvider.validateToken("not-a-real-jwt"));
    }

    @Test
    void testValidateTokenRejectsATokenSignedWithADifferentSecret() {
        JwtTokenProvider otherProvider = new JwtTokenProvider();
        // Must be a real 512-bit (64-byte decoded) key, same as this test
        // class's own @TestPropertySource secret above - HS512 rejects
        // anything shorter with a WeakKeyException, thrown here before
        // validateToken() is ever reached. (The previous secret string
        // here decoded to only 448 bits - 64 bits short.)
        otherProvider.setJwtSecret(
                "od+BZZho6EWWWAF/7rkKDxoeB9y2FaU7AY/nC2X7mlEjQw9Rx95AoSkEE+gupAu1R+qrOCFTkKYVpxF+Muf6yQ==");
        otherProvider.setJwtExpirationInMs(3600000);
        String tokenFromOtherProvider = otherProvider.generateToken("someone");

        assertFalse(jwtTokenProvider.validateToken(tokenFromOtherProvider));
    }

    @Test
    void testResolveTokenExtractsFromBearerHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer abc.def.ghi");

        assertEquals("abc.def.ghi", jwtTokenProvider.resolveToken(request));
    }

    @Test
    void testResolveTokenReturnsNullWhenHeaderMissing() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        assertNull(jwtTokenProvider.resolveToken(request));
    }

    @Test
    void testResolveTokenReturnsNullWhenNotBearerScheme() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        assertNull(jwtTokenProvider.resolveToken(request));
    }

    @Test
    void testGetAuthenticationLoadsTheUserFromTheTokenSubject() {
        String token = jwtTokenProvider.generateToken(authenticationFor("reader"));
        UserDetails loaded = new UserPrincipal(1L, "reader", null, null, "hash",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("reader")).thenReturn(loaded);

        Authentication result = jwtTokenProvider.getAuthentication(token);

        assertEquals(loaded, result.getPrincipal());
    }
}