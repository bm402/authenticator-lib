package com.bncrypted.authenticator.parser.spring.user;

import com.bncrypted.authenticator.parser.core.model.UserTokenDetails;
import com.bncrypted.authenticator.parser.core.user.HttpClientBasedUserTokenParser;
import com.bncrypted.authenticator.parser.core.user.UserTokenParser;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserTokenParserConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "userTokenParserHttpClient")
    public HttpClient userTokenParserHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public UserTokenParser<UserTokenDetails> userTokenParser(HttpClient userTokenParserHttpClient,
                                                             @Value("${authenticator.api.base-url}") String baseUrl) {

        return new HttpClientBasedUserTokenParser<>(userTokenParserHttpClient, baseUrl, UserTokenDetails.class);
    }

}
