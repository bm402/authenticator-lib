package com.bncrypted.authenticator.parser.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenCredentials {

    private String token;

    public String toJsonString() {
        return "{\"token\":\"" + getToken() + "\"}";
    }

}
