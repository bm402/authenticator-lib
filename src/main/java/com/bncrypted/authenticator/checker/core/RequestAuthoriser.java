package com.bncrypted.authenticator.checker.core;

import com.bncrypted.authenticator.checker.core.model.Subject;

import javax.servlet.http.HttpServletRequest;

public interface RequestAuthoriser<T extends Subject> {

    T authorise(HttpServletRequest request);

}
