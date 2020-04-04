package com.bncrypted.authenticator.checker.core.cache;

import com.google.common.base.Ticker;
import lombok.Setter;

@Setter
public class FakeTicker extends Ticker {

    private long time;

    public long read() {
        return time;
    }

}