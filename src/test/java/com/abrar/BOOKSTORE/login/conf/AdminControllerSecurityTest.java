package com.abrar.BOOKSTORE.login.conf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.abrar.BOOKSTORE.Login.conf.SecurityConfig;
import com.abrar.BOOKSTORE.Login.user.CustomUserDetailsService;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserPrincipal;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.controller.AdminController;
import com.abrar.BOOKSTORE.service.AccountService;
import com.abrar.BOOKSTORE.service.GenreService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Targets the specific gap called out in the code review: "no test that
 * actually verifies the property that matters most" - i.e. that
 * {@code @PreAuthorize("hasRole('LIBRARIAN')")} on AdminController, wired
 * through the real SecurityConfig filter chain, actually rejects a
 * regular ROLE_USER (not just that the annotation is present in source).
 *
 * Uses @Import(SecurityConfig.class) rather than the standalone MockMvc
 * setup the other controller tests use, specifically so the real security
 * filter chain and method-security aspect are both exercised.
 *
 * Authenticated requests here use {@link #asUser} (a real UserPrincipal,
 * via the {@code user(UserDetails)} post-processor) rather than
 * {@code @WithMockUser} - fragments/layout.html reads
 * {@code #authentication.principal.avatarUrl}, which only exists on this
 * app's own UserPrincipal, not on @WithMockUser's default
 * org.springframework.security.core.userdetails.User. That mismatch is a
 * test-setup gap only; production always goes through
 * CustomUserDetailsService, which returns a real UserPrincipal.
 */
@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerSecurityTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UserRepository userRepository;

        @MockBean
        private AccountService accountService;

        @MockBean
        private GenreService genreService;

        // Pulled in transitively by SecurityConfig - never actually exercised
        // for these GET-only/no-token requests, but must exist as beans for
        // the context to start.
        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @MockBean
        private com.abrar.BOOKSTORE.Login.auth.RateLimiter rateLimiter;

        private static RequestPostProcessor asUser(String username, String role) {
                UserDetails principal = new UserPrincipal(1L, username, username + "@example.com", null, "hash",
                                Collections.singleton(new SimpleGrantedAuthority(role)));
                return SecurityMockMvcRequestPostProcessors.user(principal);
        }

        @Test
        @WithAnonymousUser
        void testAdminUsersRedirectsAnAnonymousVisitorToLogin() throws Exception {
                mockMvc.perform(get("/admin/users"))
                                .andExpect(status().is3xxRedirection());
        }

        @Test
        void testAdminUsersRejectsARegularUser() throws Exception {
                mockMvc.perform(get("/admin/users").with(asUser("reader", "ROLE_USER")))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testAdminUsersAllowsALibrarian() throws Exception {
                when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of()));
                when(userRepository.count()).thenReturn(0L);
                when(userRepository.countByRole(anyString())).thenReturn(0L);

                mockMvc.perform(get("/admin/users").with(asUser("librarian", "ROLE_LIBRARIAN")))
                                .andExpect(status().isOk());
        }

        @Test
        void testChangeRoleRejectsARegularUserEvenWithAValidCsrfToken() throws Exception {
                // Confirms the rejection is the @PreAuthorize role check, not
                // just a missing CSRF token - a regular user with a perfectly
                // valid token still can't reach this endpoint.
                MockHttpServletRequestBuilder request = post("/admin/users/1/role")
                                .param("role", "ROLE_LIBRARIAN")
                                .with(asUser("reader", "ROLE_USER"))
                                .with(csrf());

                mockMvc.perform(request)
                                .andExpect(status().isForbidden());
        }

        @Test
        void testChangeRoleAllowsALibrarianWithAValidCsrfToken() throws Exception {
                User target = new User(2L, "reader", "hash", "ROLE_USER");
                when(userRepository.findById(2L)).thenReturn(Optional.of(target));
                when(userRepository.countByRole("ROLE_LIBRARIAN")).thenReturn(2L);

                MockHttpServletRequestBuilder request = post("/admin/users/2/role")
                                .param("role", "ROLE_LIBRARIAN")
                                .with(asUser("librarian", "ROLE_LIBRARIAN"))
                                .with(csrf());

                mockMvc.perform(request)
                                .andExpect(status().is3xxRedirection());
        }

        @Test
        void testChangeRoleRejectsAMissingCsrfTokenEvenForALibrarian() throws Exception {
                // The flip side of the test above: role alone isn't enough either -
                // CSRF protection still applies to this session-authenticated route.
                MockHttpServletRequestBuilder request = post("/admin/users/2/role")
                                .param("role", "ROLE_LIBRARIAN")
                                .with(asUser("librarian", "ROLE_LIBRARIAN"));

                mockMvc.perform(request)
                                .andExpect(status().isForbidden());
        }

        /**
         * Regression test: resetting a password used to leave an unverified
         * account unverified, so the reset looked like it succeeded but the
         * user still couldn't log in - blocked by the separate "please verify
         * your email" check instead. See AdminController#resetPassword.
         */
        @Test
        void testResetPasswordAlsoVerifiesAnUnverifiedAccount() throws Exception {
                User target = new User(2L, "reader", "old-hash", "ROLE_USER");
                target.setVerified(false);
                when(userRepository.findById(2L)).thenReturn(Optional.of(target));

                MockHttpServletRequestBuilder request = post("/admin/users/2/reset-password")
                                .param("newPassword", "brand-new-password")
                                .with(asUser("librarian", "ROLE_LIBRARIAN"))
                                .with(csrf());

                mockMvc.perform(request).andExpect(status().is3xxRedirection());

                org.mockito.ArgumentCaptor<User> saved = org.mockito.ArgumentCaptor.forClass(User.class);
                org.mockito.Mockito.verify(userRepository).save(saved.capture());
                org.junit.jupiter.api.Assertions.assertTrue(saved.getValue().isVerified());
        }

        @Test
        void testResetPasswordLeavesAnAlreadyVerifiedAccountVerified() throws Exception {
                User target = new User(2L, "reader", "old-hash", "ROLE_USER");
                target.setVerified(true);
                when(userRepository.findById(2L)).thenReturn(Optional.of(target));

                MockHttpServletRequestBuilder request = post("/admin/users/2/reset-password")
                                .param("newPassword", "brand-new-password")
                                .with(asUser("librarian", "ROLE_LIBRARIAN"))
                                .with(csrf());

                mockMvc.perform(request).andExpect(status().is3xxRedirection());

                org.mockito.ArgumentCaptor<User> saved = org.mockito.ArgumentCaptor.forClass(User.class);
                org.mockito.Mockito.verify(userRepository).save(saved.capture());
                org.junit.jupiter.api.Assertions.assertTrue(saved.getValue().isVerified());
        }

        // Same shape as the changeRole pair above, for the new genre-rename
        // route - AdminController's class-level @PreAuthorize covers every
        // method in the class, but that's worth actually proving for each
        // mutating endpoint added to it, not just assumed from the one above.
        @Test
        void testRenameGenreRejectsARegularUserEvenWithAValidCsrfToken() throws Exception {
                MockHttpServletRequestBuilder request = post("/admin/genres/1/rename")
                                .param("name", "Science Fiction")
                                .with(asUser("reader", "ROLE_USER"))
                                .with(csrf());

                mockMvc.perform(request)
                                .andExpect(status().isForbidden());
        }

        @Test
        void testRenameGenreAllowsALibrarianWithAValidCsrfToken() throws Exception {
                MockHttpServletRequestBuilder request = post("/admin/genres/1/rename")
                                .param("name", "Science Fiction")
                                .with(asUser("librarian", "ROLE_LIBRARIAN"))
                                .with(csrf());

                mockMvc.perform(request)
                                .andExpect(status().is3xxRedirection());

                org.mockito.Mockito.verify(genreService).rename(1, "Science Fiction");
        }
}