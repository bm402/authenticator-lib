package com.bncrypted.authenticator.checker.core.user;

import com.bncrypted.authenticator.checker.core.RequestAuthoriser;
import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.exception.BearerTokenInvalidException;
import com.bncrypted.authenticator.checker.core.exception.BearerTokenMissingException;
import com.bncrypted.authenticator.checker.core.exception.UnauthorisedRoleException;
import com.bncrypted.authenticator.checker.core.exception.UnauthorisedUserException;
import com.bncrypted.authenticator.checker.core.model.User;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenInvalidException;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenParsingException;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

public class UserRequestAuthoriser<T extends User> implements RequestAuthoriser<T> {

    public static final String AUTHORISATION = "Authorization";
    private final SubjectResolver<T> userResolver;
    private final Function<HttpServletRequest, Optional<String>> userIdExtractor;
    private final Function<HttpServletRequest, Collection<String>> authorisedRolesExtractor;

    public UserRequestAuthoriser(SubjectResolver<T> userResolver,
                                 Function<HttpServletRequest, Optional<String>> userIdExtractor,
                                 Function<HttpServletRequest, Collection<String>> authorisedRolesExtractor) {
        this.userResolver = userResolver;
        this.userIdExtractor = userIdExtractor;
        this.authorisedRolesExtractor = authorisedRolesExtractor;
    }

    public T authorise(HttpServletRequest request) {
        String token = request.getHeader(AUTHORISATION);
        if (token == null) {
            throw new BearerTokenMissingException();
        }

        T authenticatedUser = getTokenDetails(token);

        Collection<String> authorisedRoles = authorisedRolesExtractor.apply(request);
        if (!authorisedRoles.isEmpty() && Collections.disjoint(authorisedRoles, authenticatedUser.getRoles())) {
            throw new UnauthorisedRoleException();
        }

        userIdExtractor.apply(request).ifPresent(resourceUserId ->
                verifyRequestUserId(resourceUserId, authenticatedUser));

        return authenticatedUser;
    }

    private T getTokenDetails(String token) {
        try {
            return userResolver.getTokenDetails(token);
        } catch (UserTokenInvalidException | UserTokenParsingException ex) {
            throw new BearerTokenInvalidException(ex);
        }
    }

    private void verifyRequestUserId(String requestUserId, User authenticatedUser) throws UnauthorisedUserException {
        if (!requestUserId.equals(authenticatedUser.getPrincipal())) {
            throw new UnauthorisedUserException();
        }
    }

}
