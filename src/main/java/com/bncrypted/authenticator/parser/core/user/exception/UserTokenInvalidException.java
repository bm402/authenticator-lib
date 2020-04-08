package com.bncrypted.authenticator.parser.core.user.exception;

public class UserTokenInvalidException extends UserTokenException {

    public UserTokenInvalidException() {
        super("User token is not valid");
    }

}
