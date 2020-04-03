package com.bncrypted.authenticator.parser.core.user.exception;

public class UserTokenParsingException extends RuntimeException {

    public UserTokenParsingException(Throwable ex) {
        super("User token could not be parsed", ex);
    }

}
