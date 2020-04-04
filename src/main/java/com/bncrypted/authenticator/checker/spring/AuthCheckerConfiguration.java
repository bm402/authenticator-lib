package com.bncrypted.authenticator.checker.spring;

import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.cache.CachingSubjectResolver;
import com.bncrypted.authenticator.checker.core.model.User;
import com.bncrypted.authenticator.checker.core.user.UserRequestAuthoriser;
import com.bncrypted.authenticator.checker.core.user.UserResolver;
import com.bncrypted.authenticator.checker.spring.cache.CacheProperties;
import com.bncrypted.authenticator.parser.core.model.UserTokenDetails;
import com.bncrypted.authenticator.parser.core.user.UserTokenParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

@Configuration
public class AuthCheckerConfiguration {

    @Bean
    @ConfigurationProperties("auth.checker.cache")
    public CacheProperties cacheProperties() {
        return new CacheProperties();
    }

    @Bean
    @ConditionalOnMissingBean(name = "userResolver")
    public SubjectResolver<User> userResolver(UserTokenParser<UserTokenDetails> userTokenParser,
                                              CacheProperties cacheProperties) {

        return new CachingSubjectResolver<>(new UserResolver(userTokenParser),
                cacheProperties.getTtlInSeconds(), cacheProperties.getMaximumSize());
    }

    @Bean
    @ConditionalOnMissingBean(name = "userRequestAuthoriser")
    public UserRequestAuthoriser userRequestAuthoriser(SubjectResolver<User> userResolver,
                                                       Function<HttpServletRequest, Optional<String>> userIdExtractor,
                                                       Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor) {
        return new UserRequestAuthoriser<>(userResolver, userIdExtractor, authorizedRolesExtractor);
    }

    @Bean
    public PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider() {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new AuthCheckerUserDetailsService());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            PreAuthenticatedAuthenticationProvider preAuthenticatedAuthenticationProvider) {
        return new ProviderManager(Collections.singletonList(preAuthenticatedAuthenticationProvider));
    }

}
