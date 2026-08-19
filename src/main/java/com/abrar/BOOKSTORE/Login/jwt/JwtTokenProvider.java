//generateToken creates a JWT token with the username as its subject.
//validateToken checks the validity and integrity of a JWT token.
package com.abrar.BOOKSTORE.Login.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtTokenProvider {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    @Value("${app.jwtSecret}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    // jjwt's HS512 signing key must be at least 64 bytes - Keys.hmacShaKeyFor
    // enforces that instead of silently accepting a too-short secret the way
    // the old signWith(SignatureAlgorithm, String) overload did.
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails.getUsername());
    }

    public String generateToken(String username) {
        return buildToken(username);
    }

    private String buildToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(username)
                // A unique id per token, so a specific token can be revoked
                // (see TokenBlacklist) without needing to store the whole
                // token text or invalidate every token for the user.
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey())
                .compact();
    }

    // Extract and return the JWT token from the request header or parameter
    public String resolveToken(HttpServletRequest request) {
        // Extract token from the request header or parameter as needed
        // For example, if it's in the "Authorization" header as "Bearer <token>"
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        return null;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Get the username from the token
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Claims claims = parseClaims(authToken);
            if (tokenBlacklist.isRevoked(claims.getId())) {
                return false;
            }
            return true;
        } catch (ExpiredJwtException ex) {
            // Expired JWT token
        } catch (JwtException ex) {
            // Covers SignatureException, MalformedJwtException,
            // UnsupportedJwtException - all invalid/tampered tokens.
        } catch (IllegalArgumentException ex) {
            // JWT claims string is empty
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsernameFromToken(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    /**
     * Revokes the given (already-validated) token so it's rejected by
     * {@link #validateToken(String)} for the remainder of its natural
     * lifetime, even though it hasn't actually expired yet. Called on
     * logout - see AuthController.
     */
    public void revoke(String token) {
        Claims claims = parseClaims(token);
        tokenBlacklist.revoke(claims.getId(), claims.getExpiration().toInstant());
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public void setJwtExpirationInMs(int jwtExpirationInMs) {
        this.jwtExpirationInMs = jwtExpirationInMs;
    }
}
