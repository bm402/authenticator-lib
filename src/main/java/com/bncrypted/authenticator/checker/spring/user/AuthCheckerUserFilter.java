package com.bncrypted.authenticator.checker.spring.user;

import com.bncrypted.authenticator.checker.core.RequestAuthoriser;
import com.bncrypted.authenticator.checker.core.exception.AuthCheckerException;
import com.bncrypted.authenticator.checker.core.model.User;
import com.bncrypted.authenticator.checker.core.user.UserRequestAuthoriser;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import javax.servlet.http.HttpServletRequest;

public class AuthCheckerUserFilter<T extends User> extends AbstractPreAuthenticatedProcessingFilter {

    private final RequestAuthoriser<T> userRequestAuthoriser;

    public AuthCheckerUserFilter(RequestAuthoriser<T> userRequestAuthoriser) {
        this.userRequestAuthoriser = userRequestAuthoriser;
    }

    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return authoriseUser(request);
    }

    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getHeader(UserRequestAuthoriser.AUTHORISATION);
    }

    private T authoriseUser(HttpServletRequest request) {
        try {
            return userRequestAuthoriser.authorise(request);
        } catch (AuthCheckerException | UserTokenException ex) {
            return null;
        }
    }

}
