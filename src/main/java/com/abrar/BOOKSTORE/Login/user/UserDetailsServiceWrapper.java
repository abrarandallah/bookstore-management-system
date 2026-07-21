package com.abrar.BOOKSTORE.Login.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceWrapper implements AuthenticationUserDetailsService {

    private final UserDetailsService userDetailsService;

    public UserDetailsServiceWrapper(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    @Override
    public UserDetails loadUserDetails(Authentication token) throws UsernameNotFoundException {
        return userDetailsService.loadUserByUsername(token.getDetails().toString());
    }
}
