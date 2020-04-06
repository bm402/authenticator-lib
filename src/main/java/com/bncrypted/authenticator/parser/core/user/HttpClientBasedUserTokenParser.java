package com.bncrypted.authenticator.parser.core.user;

import com.bncrypted.authenticator.parser.core.model.TokenCredentials;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenInvalidException;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenParsingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;

public class HttpClientBasedUserTokenParser<T> implements UserTokenParser<T> {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<T> type;

    public HttpClientBasedUserTokenParser(HttpClient httpClient, String baseUrl, Class<T> type) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
        this.type = type;
    }

    public T parse(String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        TokenCredentials tokenCredentials = new TokenCredentials(token);

        try {
            HttpPost request = new HttpPost(baseUrl + "/verify");
            request.setHeader("Content-Type", "application/json");
            request.setEntity(new StringEntity(tokenCredentials.toJsonString()));

            return httpClient.execute(request, httpResponse -> {
                checkStatusIs2xx(httpResponse);
                return objectMapper.readValue(httpResponse.getEntity().getContent(), type);
            });
        } catch (IOException ex) {
            throw new UserTokenParsingException(ex);
        }
    }

    private void checkStatusIs2xx(HttpResponse httpResponse) throws IOException {
        int status = httpResponse.getStatusLine().getStatusCode();
        if (status == 401) {
            throw new UserTokenInvalidException();
        } else if (status / 100 != 2) {
            throw new ClientProtocolException("Unexpected response status: " + status);
        }
    }

}
