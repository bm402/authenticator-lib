package com.bncrypted.authenticator.checker.spring.user;

import com.bncrypted.authenticator.checker.core.model.User;
import com.bncrypted.authenticator.checker.spring.helper.UserResolverHelper;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;
import java.util.UUID;

import static com.bncrypted.authenticator.checker.core.user.UserRequestAuthoriser.AUTHORISATION;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class UserTokenTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserResolverHelper userResolverHelper;

    private String token;
    private String validUsername = "test-user";
    private Set<String> validRoles = ImmutableSet.of("user");

    @BeforeEach
    void setup() {
        token = UUID.randomUUID().toString();
    }

    @Test
    void validTokenShouldBeValidated() {
        User matchingUser = new User(validUsername, validRoles);
        userResolverHelper.registerToken(token, matchingUser);

        ResponseEntity<String> response = restTemplate.exchange("/test", GET,
                withAuthorisationHeader(token), String.class);

        assertNotNull(response);
        assertAll("response",
                () -> assertEquals(OK, response.getStatusCode()),
                () -> assertEquals("test-user", response.getBody())
        );
    }

    @Test
    void missingTokenShouldNotBeValidated() {
        ResponseEntity<String> response = restTemplate.getForEntity("/test", String.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void unknownTokenShouldNotBeValidated() {
        String unknownToken = UUID.randomUUID().toString();
        ResponseEntity<String> response = restTemplate.exchange("/test", GET,
                withAuthorisationHeader(unknownToken), String.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void mismatchedUsernameShouldNotBeValidated() {
        String invalidUsername = UUID.randomUUID().toString();
        User userWithMismatchedUsername = new User(invalidUsername, validRoles);
        userResolverHelper.registerToken(token, userWithMismatchedUsername);

        ResponseEntity<String> response = restTemplate.exchange("/test", GET,
                withAuthorisationHeader(token), String.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void mismatchedRolesShouldNotBeValidated() {
        Set<String> invalidRoles = ImmutableSet.of(UUID.randomUUID().toString());
        User userWithMismatchedRoles = new User(validUsername, invalidRoles);
        userResolverHelper.registerToken(token, userWithMismatchedRoles);

        ResponseEntity<String> response = restTemplate.exchange("/test", GET,
                withAuthorisationHeader(token), String.class);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    private HttpEntity<Object> withAuthorisationHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORISATION, token);
        return new HttpEntity<>(headers);
    }

}
