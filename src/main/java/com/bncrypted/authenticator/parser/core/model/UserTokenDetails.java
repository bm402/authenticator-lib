package com.bncrypted.authenticator.parser.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UserTokenDetails {

    private String id;
    private Set<String> roles;

    @JsonCreator
    public UserTokenDetails(@JsonProperty("id") String id, @JsonProperty("roles") Set<String> roles) {
        this.id = id;
        this.roles = roles;
    }

}
