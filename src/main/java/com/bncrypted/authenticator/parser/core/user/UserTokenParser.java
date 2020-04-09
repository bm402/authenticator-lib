package com.bncrypted.authenticator.parser.core.user;

public interface UserTokenParser<T> {

    /**
     * Parses a user token to extract the details that have been placed
     * inside. If the token is invalid, an error is thrown.
     *
     * @param token a user token
     * @return a details object that was placed inside the token
     */
    T parse(String token);

}
