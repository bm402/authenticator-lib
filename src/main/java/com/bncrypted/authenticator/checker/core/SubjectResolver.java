package com.bncrypted.authenticator.checker.core;

import com.bncrypted.authenticator.checker.core.model.Subject;

public interface SubjectResolver<T extends Subject> {

    T getTokenDetails(String token);

}
