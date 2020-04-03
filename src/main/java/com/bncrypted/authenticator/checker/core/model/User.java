package com.bncrypted.authenticator.checker.core.model;

import lombok.Getter;

import java.util.Set;

@Getter
public class User extends Subject {

    private Set<String> roles;

    public User(String principalId, Set<String> roles) {
        super(principalId);
        this.roles = roles;
    }

}
