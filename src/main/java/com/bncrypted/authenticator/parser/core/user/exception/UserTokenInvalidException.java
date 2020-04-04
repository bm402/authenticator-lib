package com.bncrypted.authenticator.parser.core.user.exception;

public class UserTokenInvalidException extends RuntimeException {

    public UserTokenInvalidException() {
        super("User token is not valid");
    }

}
