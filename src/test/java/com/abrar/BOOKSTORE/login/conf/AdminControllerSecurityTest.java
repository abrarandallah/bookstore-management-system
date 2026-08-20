package com.abrar.BOOKSTORE.login.conf;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.abrar.BOOKSTORE.Login.conf.SecurityConfig;
import com.abrar.BOOKSTORE.Login.jwt.JwtTokenProvider;
import com.abrar.BOOKSTORE.Login.user.CustomUserDetailsService;
import com.abrar.BOOKSTORE.Login.user.User;
import com.abrar.BOOKSTORE.Login.user.UserRepository;
import com.abrar.BOOKSTORE.controller.AdminController;
import com.abrar.BOOKSTORE.service.AccountService;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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

    // Pulled in transitively by SecurityConfig - never actually exercised
    // for these GET-only/no-token requests, but must exist as beans for
    // the context to start.
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.abrar.BOOKSTORE.Login.auth.RateLimiter rateLimiter;

    @Test
    @WithAnonymousUser
    void testAdminUsersRedirectsAnAnonymousVisitorToLogin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "reader", roles = "USER")
    void testAdminUsersRejectsARegularUser() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "librarian", roles = "LIBRARIAN")
    void testAdminUsersAllowsALibrarian() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "reader", roles = "USER")
    void testChangeRoleRejectsARegularUserEvenWithAValidCsrfToken() throws Exception {
        // Confirms the rejection is the @PreAuthorize role check, not
        // just a missing CSRF token - a regular user with a perfectly
        // valid token still can't reach this endpoint.
        mockMvc.perform(post("/admin/users/1/role")
                .param("role", "ROLE_LIBRARIAN")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "librarian", roles = "LIBRARIAN")
    void testChangeRoleAllowsALibrarianWithAValidCsrfToken() throws Exception {
        User target = new User(2L, "reader", "hash", "ROLE_USER");
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.countByRole("ROLE_LIBRARIAN")).thenReturn(2L);

        mockMvc.perform(post("/admin/users/2/role")
                .param("role", "ROLE_LIBRARIAN")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "librarian", roles = "LIBRARIAN")
    void testChangeRoleRejectsAMissingCsrfTokenEvenForALibrarian() throws Exception {
        // The flip side of the test above: role alone isn't enough either -
        // CSRF protection still applies to this session-authenticated route.
        mockMvc.perform(post("/admin/users/2/role")
                .param("role", "ROLE_LIBRARIAN"))
                .andExpect(status().isForbidden());
    }
}