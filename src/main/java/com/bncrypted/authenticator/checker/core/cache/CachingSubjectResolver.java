package com.bncrypted.authenticator.checker.core.cache;

import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.model.Subject;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

public class CachingSubjectResolver<T extends Subject> implements SubjectResolver<T> {

    private final LoadingCache<String, T> subjectCache;

    public CachingSubjectResolver(SubjectResolver<T> delegate, int ttlInSeconds, int maximumSize) {
        this(delegate, ttlInSeconds, maximumSize, Ticker.systemTicker());
    }

    public CachingSubjectResolver(SubjectResolver<T> delegate, int ttlInSeconds, int maximumSize, Ticker ticker) {
        this.subjectCache = CacheBuilder.newBuilder()
                .maximumSize(maximumSize)
                .ticker(ticker)
                .expireAfterWrite(ttlInSeconds, TimeUnit.SECONDS)
                .build(CacheLoader.from(delegate::getTokenDetails));
    }

    public T getTokenDetails(String token) {
        return subjectCache.getUnchecked(token);
    }

}
