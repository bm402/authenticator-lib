package com.bncrypted.authenticator.parser.core.user.exception;

public abstract class UserTokenException extends RuntimeException {

    public UserTokenException(String message) {
        super(message);
    }

    public UserTokenException(String message, Throwable ex) {
        super(message, ex);
    }

}
