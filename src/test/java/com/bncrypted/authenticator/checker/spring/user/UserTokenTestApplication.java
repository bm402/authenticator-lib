package com.bncrypted.authenticator.checker.spring.user;

import com.bncrypted.authenticator.checker.core.RequestAuthoriser;
import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.model.User;
import com.bncrypted.authenticator.checker.spring.AuthCheckerConfiguration;
import com.bncrypted.authenticator.checker.spring.helper.UserResolverHelper;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

@SpringBootApplication
public class UserTokenTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserTokenTestApplication.class, args);
    }

    @RestController
    public static class TestController {

        @GetMapping("/test")
        public String testEndpoint() {
            UserDetails details = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return details.getUsername();
        }

    }

    @Configuration
    public class AuthCheckerTestConfiguration {

        @Bean
        public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
            return (request) -> Optional.of("test-user");
        }

        @Bean
        public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
            return (any) -> ImmutableSet.of("user");
        }

        @Bean
        public SubjectResolver<User> userResolver() {
            return new UserResolverHelper();
        }

        @Bean
        public AuthCheckerUserFilter authCheckerUserFilter(RequestAuthoriser<User> userAuthClient,
                                                           AuthenticationManager authenticationManager) {

            AuthCheckerUserFilter filter = new AuthCheckerUserFilter<>(userAuthClient);
            filter.setAuthenticationManager(authenticationManager);
            return filter;
        }

    }

    @Configuration
    @EnableWebSecurity
    public class SecurityTestConfiguration extends WebSecurityConfigurerAdapter {

        @Autowired
        private AuthCheckerUserFilter filter;

        protected void configure(HttpSecurity http) throws Exception {
            http.addFilter(filter)
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated();
        }

    }

}
