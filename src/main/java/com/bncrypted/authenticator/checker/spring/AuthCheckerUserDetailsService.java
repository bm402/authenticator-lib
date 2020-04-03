package com.bncrypted.authenticator.checker.spring;

import com.bncrypted.authenticator.checker.core.model.User;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class AuthCheckerUserDetailsService implements
        AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) {
        Object principal = token.getPrincipal();

        User user = (User) principal;
        return new com.bncrypted.authenticator.checker.spring.user.UserDetails(
                user.getPrincipal(), String.valueOf(token.getCredentials()), user.getRoles());
    }
}
