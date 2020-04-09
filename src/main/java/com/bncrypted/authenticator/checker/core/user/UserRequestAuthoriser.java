package com.bncrypted.authenticator.checker.core.user;

import com.bncrypted.authenticator.checker.core.RequestAuthoriser;
import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.exception.BearerTokenInvalidException;
import com.bncrypted.authenticator.checker.core.exception.BearerTokenMissingException;
import com.bncrypted.authenticator.checker.core.exception.UnauthorisedRoleException;
import com.bncrypted.authenticator.checker.core.exception.UnauthorisedUserException;
import com.bncrypted.authenticator.checker.core.model.User;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenException;

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

    /**
     * {@inheritDoc}
     *
     * This implementation requires an Authorization HTTP header to be provided
     * which contains a bearer token. The bearer token should contain the user ID
     * and associated roles of the user trying to access the resource.
     *
     * The token is verified using a {@link UserResolver}, and the user-based and
     * role-based access conditions defined in the {@link #userIdExtractor} and
     * {@link #authorisedRolesExtractor} configuration are evaluated.
     *
     * If the user meets these conditions, the user is authenticated and their
     * details are returned. Otherwise, an exception is thrown.
     *
     * @param  request a HTTP servlet request
     * @return the user ID and associated roles of the authorised user
     */
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
        } catch (UserTokenException ex) {
            throw new BearerTokenInvalidException(ex);
        }
    }

    private void verifyRequestUserId(String requestUserId, User authenticatedUser) {
        if (!requestUserId.equals(authenticatedUser.getPrincipal())) {
            throw new UnauthorisedUserException();
        }
    }

}
