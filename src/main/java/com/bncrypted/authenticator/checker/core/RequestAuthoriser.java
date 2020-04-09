package com.bncrypted.authenticator.checker.core;

import com.bncrypted.authenticator.checker.core.model.Subject;

import javax.servlet.http.HttpServletRequest;

public interface RequestAuthoriser<T extends Subject> {

    /**
     * Decides whether or not the request is authorised to access a
     * given resource.
     *
     * @param  request a HTTP servlet request
     * @return         the identity of an authorised subject
     */
    T authorise(HttpServletRequest request);

}
