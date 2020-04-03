package com.bncrypted.authenticator.checker.core.exception;

public class BearerTokenMissingException extends AuthCheckerException {

    public BearerTokenMissingException() {
        super("Bearer token missing");
    }

}
