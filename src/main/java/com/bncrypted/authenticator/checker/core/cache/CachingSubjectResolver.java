package com.bncrypted.authenticator.checker.core.cache;

import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.exception.BearerTokenInvalidException;
import com.bncrypted.authenticator.checker.core.model.Subject;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import lombok.SneakyThrows;

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

    @SneakyThrows
    public T getTokenDetails(String token) {
        try {
            return subjectCache.getUnchecked(token);
        } catch (UncheckedExecutionException ex) {
            throw ex.getCause();
        }
    }

}
