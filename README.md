# Authenticator library

[![GitHub Actions Status](https://github.com/bncrypted/authenticator-lib/workflows/build/badge.svg)](https://github.com/bncrypted/authenticator-lib/actions?query=workflow%3Abuild)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bncrypted_authenticator-lib&metric=alert_status)](https://sonarcloud.io/dashboard?id=bncrypted_authenticator-lib)

A library for configuring JWT-based user authentication for Spring Boot applications using Spring Security:
- provides coarse-grained user and role-based access control which can be customised based on the needs of
the application
- uses the [Authenticator API](https://github.com/bncrypted/authenticator-api) to manage authentication
token leasing and verification
- provides basic Spring Boot and Spring Security configuration for HTTP authentication which can be extended if
necessary
- supports caching for efficient authentication token lookup

## Securing a Spring Boot application

### Importing the Authenticator library

The library is published on the GitHub Package Registry, and can be imported into your application using Maven
or Gradle as required.

[https://github.com/bncrypted/authenticator-lib/packages](https://github.com/bncrypted/authenticator-lib/packages)

### Configuring the filter

The library provides an authentication filter which can be used to determine whether the holder of an authentication
token has access to a particular resource. This filter can be configured by initialising two extractor beans: one
for defining which users can access the resource and another for defining which roles can access the resource. The
resource is described by the HTTP request, so the extractors can only use information in the request (eg. URI, HTTP
headers) to determine which users/roles should be able to access that resource.

```java
@Configuration
public class AuthCheckerConfiguration {

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        Pattern userPattern = Pattern.compile("^/users/([^/]+)/.+$");
        return request -> {
            Matcher userMatcher = userPattern.matcher(request.getRequestURI());
            boolean isUserMatched = userMatcher.find();
            return Optional.ofNullable(isUserMatched ? userMatcher.group(1) : null);
        };
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        Pattern adminPattern = Pattern.compile("^/admin/.*");
        return request -> {
            Matcher adminMatcher = adminPattern.matcher(request.getRequestURI());
            boolean isAdminMatched = adminMatcher.find();
            return isAdminMatched ? ImmutableSet.of("admin") : ImmutableSet.of();
        };
    }

}
```

#### User ID extractor

The user ID extractor determines whether user-based access control should be applied to a resource. In the example
above, the extractor checks the request URI to see whether the user is trying to access a resource that only the
owner of the resource should have access to (eg. a user profile). If the request URI indicates that the resource is
private to a single user, it extracts the user ID from the URI and tells the authentication filter that only this 
user ID should be able to access this resource. Otherwise, null is returned which indicates that the resource should
be accessible to all users (provided they hold an authorised role).

#### Authorised roles extractor

The authorised roles extractor determines whether role-based access control should be applied to a resource. It
works in a similar way to the user ID extractor, but it returns a collection of authorised roles rather than a
specific user ID. In the example above, the extractor checks the request URI to see whether the user is trying to
access an admin-only resource. If the request URI indicates that the resource is admin-only, the authentication
filter is told that only users with the admin role should be able to access this resource. Otherwise, an empty
collection is returned which indicates that the resource should be accessible to all users. 

Note that role-based access control can be coarse-grained at this level; for more fine-grained access control,
standard Spring Security approaches can be used (eg. @Secured) at the class/method level.

### Configuring Spring Security

To add the authentication filter to the Spring Security configuration of the application, an additional
configuration class is required.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthCheckerUserFilter filter;

    protected void configure(HttpSecurity http) throws Exception {
        http.addFilter(filter)
                .sessionManagement().sessionCreationPolicy(STATELESS).and()
                .authorizeRequests()
                .anyRequest()
                .authenticated();
    }

}
```

This configuration class takes the authentication filter that has been initialised by the library and adds it to
the Spring Security configuration of the application. Other configuration is also added to ensure that all
HTTP requests to the application are authorised and authenticated, and that the default Spring Security session
caching is disabled (token caching is handled by the library).

### Configuring the cache

The library uses a Google Guava LoadingCache which aims to minimise latency and reduce the number of HTTP requests
being sent to the Authenticator API when authenticating a token. The following application properties can be defined
to configure the cache.

```properties
authenticator.lib.cache.ttl-in-seconds=60
authenticator.lib.cache.maximum-size=200
``` 

The cache can be disabled by removing these application properties or by setting either to zero.

### Configuring the connection to the Authenticator API

The library uses the [Authenticator API](https://github.com/bncrypted/authenticator-api) to provide authentication
token verification. The location of the deployed Authenticator API must be defined using the following application
property.

```properties
authenticator.api.base-url=http://localhost:5000
```

## Using the secured application

Once the application has been secured by the library and the configured authentication filter, all resources in
application require user authentication in order to be retrieved (unless the security configuration specifies that
access to only certain resources will be controlled).

To do this, the user must provide a JWT-based bearer token in the Authorization HTTP header. This token must have
been generated by the [Authenticator API](https://github.com/bncrypted/authenticator-api).

```
"Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.Xpk63zUfXiI5f_bdGjqrhx03aGyBn9ETgXbkAgLalPk"
```

When the HTTP request is received by the application, the bearer token is verified by the library (and if not
cached, the Authenticator API). If the token is valid and contains a user/role combination that is allowed to
access the requested resource, the resource will be returned. If the token is invalid, expired, or contains a
user/role combination that should not have access to the requested resource, then a HTTP 403 Forbidden error will
be returned.
