package com.bncrypted.authenticator.checker.spring.cache;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CacheProperties {

    private int ttlInSeconds;
    private int maximumSize;

    public CacheProperties() {
        this.ttlInSeconds = 0;
        this.maximumSize = 0;
    }

}
