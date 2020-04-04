package com.bncrypted.authenticator.checker.core.exception;

public abstract class AuthCheckerException extends RuntimeException {

    public AuthCheckerException(String message) {
        super(message);
    }

    public AuthCheckerException(Throwable ex) {
        super(ex);
    }

}
