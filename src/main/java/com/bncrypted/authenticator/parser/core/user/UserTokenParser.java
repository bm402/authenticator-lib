package com.bncrypted.authenticator.parser.core.user;

public interface UserTokenParser<T> {

    T parse(String token);

}
