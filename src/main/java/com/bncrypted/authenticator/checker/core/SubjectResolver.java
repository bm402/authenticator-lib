package com.bncrypted.authenticator.checker.core;

import com.bncrypted.authenticator.checker.core.model.Subject;

public interface SubjectResolver<T extends Subject> {

    /**
     * Identifies a subject based on a token.
     *
     * @param token a token
     * @return the subject that the token is based on
     */
    T getTokenDetails(String token);

}
