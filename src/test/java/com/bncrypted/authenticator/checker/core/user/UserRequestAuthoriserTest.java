package com.bncrypted.authenticator.checker.core.user;

import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.exception.BearerTokenInvalidException;
import com.bncrypted.authenticator.checker.core.exception.BearerTokenMissingException;
import com.bncrypted.authenticator.checker.core.exception.UnauthorisedRoleException;
import com.bncrypted.authenticator.checker.core.exception.UnauthorisedUserException;
import com.bncrypted.authenticator.checker.core.model.User;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenInvalidException;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import static com.bncrypted.authenticator.checker.core.user.UserRequestAuthoriser.AUTHORISATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRequestAuthoriserTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private SubjectResolver<User> mockSubjectResolver;

    private final String validBearerToken = "Bearer valid.bearer.token";
    private final String validUsername = "test-user";
    private final Set<String> validRoles = ImmutableSet.of("user");
    private final Function<HttpServletRequest, Optional<String>> userIdExtractor = (any) -> Optional.of(validUsername);
    private final Function<HttpServletRequest, Collection<String>> authorisedRolesExtractor = (any) -> validRoles;

    @Test
    void whenUserHasValidUserIdAndAuthorisedRole_thenShouldBeAuthorised() {
        User stubbedUser = new User(validUsername, validRoles);

        when(mockRequest.getHeader(AUTHORISATION)).thenReturn(validBearerToken);
        when(mockSubjectResolver.getTokenDetails(validBearerToken)).thenReturn(stubbedUser);

        UserRequestAuthoriser userRequestAuthoriser = new UserRequestAuthoriser<>(mockSubjectResolver,
                userIdExtractor, authorisedRolesExtractor);
        User actualUser = userRequestAuthoriser.authorise(mockRequest);

        assertThat(actualUser).isEqualToComparingFieldByField(stubbedUser);
    }

    @Test
    void whenUserHasAuthorisedRoleAndThereIsNoUserValidationInRequest_thenShouldBeAuthorised() {
        Function<HttpServletRequest, Optional<String>> emptyUserIdExtractor = (any) -> Optional.empty();
        String randomUsername = UUID.randomUUID().toString();
        User stubbedUser = new User(randomUsername, validRoles);

        when(mockRequest.getHeader(AUTHORISATION)).thenReturn(validBearerToken);
        when(mockSubjectResolver.getTokenDetails(validBearerToken)).thenReturn(stubbedUser);

        UserRequestAuthoriser userRequestAuthoriser = new UserRequestAuthoriser<>(mockSubjectResolver,
                emptyUserIdExtractor, authorisedRolesExtractor);
        User actualUser = userRequestAuthoriser.authorise(mockRequest);

        assertThat(actualUser).isEqualToComparingFieldByField(stubbedUser);
    }

    @Test
    void whenUserHasValidUserIdAndThereIsNoRoleAuthorisationInRequest_thenShouldBeAuthorised() {
        Function<HttpServletRequest, Collection<String>> emptyAuthorisedRolesExtractor = (any) -> ImmutableSet.of();
        Set<String> randomRoles = ImmutableSet.of(UUID.randomUUID().toString());
        User stubbedUser = new User(validUsername, randomRoles);

        when(mockRequest.getHeader(AUTHORISATION)).thenReturn(validBearerToken);
        when(mockSubjectResolver.getTokenDetails(validBearerToken)).thenReturn(stubbedUser);

        UserRequestAuthoriser userRequestAuthoriser = new UserRequestAuthoriser<>(mockSubjectResolver,
                userIdExtractor, emptyAuthorisedRolesExtractor);
        User actualUser = userRequestAuthoriser.authorise(mockRequest);

        assertThat(actualUser).isEqualToComparingFieldByField(stubbedUser);
    }

    @Test
    void whenUserHasValidUserIdButNoAuthorisedRole_thenShouldNotBeAuthorised() {
        Set<String> invalidRoles = ImmutableSet.of(UUID.randomUUID().toString());
        User stubbedUser = new User(validUsername, invalidRoles);

        when(mockRequest.getHeader(AUTHORISATION)).thenReturn(validBearerToken);
        when(mockSubjectResolver.getTokenDetails(validBearerToken)).thenReturn(stubbedUser);

        UserRequestAuthoriser userRequestAuthoriser = new UserRequestAuthoriser<>(mockSubjectResolver,
                userIdExtractor, authorisedRolesExtractor);

        assertThrows(UnauthorisedRoleException.class, () -> userRequestAuthoriser.authorise(mockRequest));
    }

    @Test
    void whenUserHasAuthorisedRoleButInvalidUserId_thenShouldNotBeAuthorised() {
        String invalidUsername = UUID.randomUUID().toString();
        User stubbedUser = new User(invalidUsername, validRoles);

        when(mockRequest.getHeader(AUTHORISATION)).thenReturn(validBearerToken);
        when(mockSubjectResolver.getTokenDetails(validBearerToken)).thenReturn(stubbedUser);

        UserRequestAuthoriser userRequestAuthoriser = new UserRequestAuthoriser<>(mockSubjectResolver,
                userIdExtractor, authorisedRolesExtractor);

        assertThrows(UnauthorisedUserException.class, () -> userRequestAuthoriser.authorise(mockRequest));
    }

    @Test
    void whenInvalidToken_thenShouldNotBeAuthorised() {
        String invalidBearerToken = UUID.randomUUID().toString();

        when(mockRequest.getHeader(AUTHORISATION)).thenReturn(invalidBearerToken);
        when(mockSubjectResolver.getTokenDetails(invalidBearerToken)).thenThrow(
                new BearerTokenInvalidException(new UserTokenInvalidException()));

        UserRequestAuthoriser userRequestAuthoriser = new UserRequestAuthoriser<>(mockSubjectResolver,
                userIdExtractor, authorisedRolesExtractor);

        assertThrows(BearerTokenInvalidException.class, () -> userRequestAuthoriser.authorise(mockRequest));
    }

    @Test
    void whenMissingToken_thenShouldNotBeAuthorised() {
        when(mockRequest.getHeader(AUTHORISATION)).thenReturn(null);

        UserRequestAuthoriser userRequestAuthoriser = new UserRequestAuthoriser<>(mockSubjectResolver,
                userIdExtractor, authorisedRolesExtractor);

        assertThrows(BearerTokenMissingException.class, () -> userRequestAuthoriser.authorise(mockRequest));
    }

}
