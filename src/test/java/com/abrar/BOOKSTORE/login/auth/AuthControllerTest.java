package com.abrar.BOOKSTORE.login.auth;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.abrar.BOOKSTORE.Login.auth.AuthController;
import com.abrar.BOOKSTORE.Login.auth.AuthExceptionHandler;
import com.abrar.BOOKSTORE.Login.auth.AuthService;
import com.abrar.BOOKSTORE.Login.conf.SignupRequest;
import com.abrar.BOOKSTORE.Login.jwt.JwtTokenProvider;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ContextConfiguration(classes = { AuthController.class })
@ExtendWith(SpringExtension.class)
class AuthControllerTest {

        @Autowired
        private AuthController authController;

        @MockBean
        private AuthenticationManager authenticationManager;
        @MockBean
        private JwtTokenProvider jwtTokenProvider;
        @MockBean
        private UserRepository userRepository;
        @MockBean
        private PasswordEncoder passwordEncoder;
        @MockBean
        private AuthService authService;

        private final ObjectMapper mapper = new ObjectMapper();

        private org.springframework.test.web.servlet.MockMvc mockMvc() {
                return MockMvcBuilders.standaloneSetup(authController)
                                .setControllerAdvice(new AuthExceptionHandler())
                                // standaloneSetup doesn't auto-configure a Validator the
                                // way a full Spring Boot context does, so @Valid on
                                // SignupRequest/LoginRequest was silently doing nothing -
                                // blank-field requests sailed straight through into the
                                // controller body instead of being rejected with 400.
                                .setValidator(new LocalValidatorFactoryBean())
                                .build();
        }

        @Test
        void testSignupSucceeds() throws Exception {
                doNothing().when(authService).registerUser(Mockito.any());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(
                                                new SignupRequest("reader", "reader@example.com", "password123")));

                mockMvc().perform(request)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true));
        }

        @Test
        void testSignupRejectsBlankFields() throws Exception {
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(new SignupRequest("", "", "")));

                mockMvc().perform(request)
                                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
                Mockito.verify(authService, Mockito.never()).registerUser(Mockito.any());
        }

        @Test
        void testSignupRejectsDuplicateUsernameOrEmail() throws Exception {
                doThrow(new AuthenticationServiceException("Username or email is already in use."))
                                .when(authService).registerUser(Mockito.any());

                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(
                                                new SignupRequest("reader", "reader@example.com", "password123")));

                mockMvc().perform(request)
                                .andExpect(MockMvcResultMatchers.status().isConflict())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                                .value("Username or email is already in use."));
        }

        @Test
        void testLoginSucceedsAndReturnsAToken() throws Exception {
                Authentication authentication = new UsernamePasswordAuthenticationToken("reader", "password123");
                when(authenticationManager.authenticate(Mockito.any())).thenReturn(authentication);
                when(jwtTokenProvider.generateToken(authentication)).thenReturn("a.b.c");

                String body = "{\"usernameOrEmail\":\"reader\",\"password\":\"password123\"}";
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body);

                mockMvc().perform(request)
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value("a.b.c"))
                                .andExpect(MockMvcResultMatchers.jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        void testLoginRejectsBadCredentials() throws Exception {
                when(authenticationManager.authenticate(Mockito.any()))
                                .thenThrow(new BadCredentialsException("Bad credentials"));

                String body = "{\"usernameOrEmail\":\"reader\",\"password\":\"wrong-password\"}";
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body);

                mockMvc().perform(request)
                                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                                                .value("Invalid username or password."));
        }

        @Test
        void testLoginRejectsBlankFields() throws Exception {
                String body = "{\"usernameOrEmail\":\"\",\"password\":\"\"}";
                MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body);

                mockMvc().perform(request)
                                .andExpect(MockMvcResultMatchers.status().isBadRequest());
                Mockito.verify(authenticationManager, Mockito.never()).authenticate(Mockito.any());
        }
}