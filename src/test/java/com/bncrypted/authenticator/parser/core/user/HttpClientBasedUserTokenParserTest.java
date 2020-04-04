package com.bncrypted.authenticator.parser.core.user;

import com.bncrypted.authenticator.parser.core.model.UserTokenDetails;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenInvalidException;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenParsingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.collect.ImmutableSet;
import org.apache.http.impl.client.HttpClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpClientBasedUserTokenParserTest {

    private static final String VALID_RESPONSE = "{\"id\":\"test-user\",\"roles\":[\"user\"],\"name\":\"test\"}";

    private WireMockServer wireMockServer;
    private HttpClientBasedUserTokenParser<UserTokenDetails> parser;

    @BeforeEach
    void setup() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        parser = new HttpClientBasedUserTokenParser<>(HttpClients.createDefault(),
                "http://localhost:" + wireMockServer.port(), UserTokenDetails.class);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void whenTokenIsValid_thenTokenDetailsShouldBeReturned() {
        stubFor(post(urlEqualTo("/verify")).withRequestBody(equalToJson("{\"token\":\"validToken\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(VALID_RESPONSE)
                )
        );

        UserTokenDetails expectedUserTokenDetails = new UserTokenDetails("test-user", ImmutableSet.of("user"));
        UserTokenDetails actualUserTokenDetails = parser.parse("Bearer validToken");

        assertThat(actualUserTokenDetails).isEqualToComparingFieldByField(expectedUserTokenDetails);
    }

    @Test
    void whenTokenIsInvalid_thenTokenParsingExceptionShouldBeThrown() {
        stubFor(post("/verify").withRequestBody(equalToJson("{\"token\":\"invalidToken\"}"))
                .willReturn(aResponse()
                        .withStatus(400)
                )
        );

        assertThrows(UserTokenParsingException.class, () -> parser.parse("Bearer invalidToken"));
    }

    @Test
    void whenTokenIsExpired_thenTokenInvalidExceptionShouldBeThrown() {
        stubFor(post("/verify").withRequestBody(equalToJson("{\"token\":\"expiredToken\"}"))
                .willReturn(aResponse()
                        .withStatus(401)
                )
        );

        assertThrows(UserTokenInvalidException.class, () -> parser.parse("Bearer expiredToken"));
    }

}
