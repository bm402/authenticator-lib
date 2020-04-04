package com.bncrypted.authenticator.checker.core.cache;

import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.model.Subject;
import com.bncrypted.authenticator.checker.core.model.User;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CachingSubjectResolverTest {

    @Mock
    private SubjectResolver<Subject> mockDelegate;

    private final FakeTicker fakeTicker = new FakeTicker();
    private String token;

    @BeforeEach
    void setup() {
        token = UUID.randomUUID().toString();
        Subject subject = new User("test-user", ImmutableSet.of("user"));
        when(mockDelegate.getTokenDetails(anyString())).thenReturn(subject);
    }

    @Test
    void whenTtlHasNotPassed_thenShouldNotCallDelegateAgain() {
        CachingSubjectResolver cachingSubjectResolver = new CachingSubjectResolver<>(mockDelegate, 5, 2, fakeTicker);

        cachingSubjectResolver.getTokenDetails(token);
        verify(mockDelegate, times(1)).getTokenDetails(token);

        cachingSubjectResolver.getTokenDetails(token);
        verifyNoMoreInteractions(mockDelegate);
    }

    @Test
    void whenTtlHasPassed_thenShouldCallDelegateAgain() {
        CachingSubjectResolver cachingSubjectResolver = new CachingSubjectResolver<>(mockDelegate, 5, 2, fakeTicker);

        cachingSubjectResolver.getTokenDetails(token);
        verify(mockDelegate, times(1)).getTokenDetails(token);

        fakeTicker.setTime(SECONDS.toNanos(10));

        cachingSubjectResolver.getTokenDetails(token);
        verify(mockDelegate, times(2)).getTokenDetails(token);
    }

    @Test
    void whenCacheReachesMaximumCapacity_thenItShouldCallDelegateAsRequired() {
        CachingSubjectResolver cachingSubjectResolver = new CachingSubjectResolver<>(mockDelegate, 5, 2, fakeTicker);

        cachingSubjectResolver.getTokenDetails("tokenA");
        cachingSubjectResolver.getTokenDetails("tokenB");
        cachingSubjectResolver.getTokenDetails("tokenC");
        cachingSubjectResolver.getTokenDetails("tokenD");
        cachingSubjectResolver.getTokenDetails("tokenC");
        cachingSubjectResolver.getTokenDetails("tokenD");
        cachingSubjectResolver.getTokenDetails("tokenA");
        cachingSubjectResolver.getTokenDetails("tokenB");

        verify(mockDelegate, times(2)).getTokenDetails("tokenA");
        verify(mockDelegate, times(2)).getTokenDetails("tokenB");
        verify(mockDelegate, times(1)).getTokenDetails("tokenC");
        verify(mockDelegate, times(1)).getTokenDetails("tokenD");
    }

    @Test
    void whenTtlIsSetToZero_thenCachingShouldBeDisabled() {
        CachingSubjectResolver cachingSubjectResolver = new CachingSubjectResolver<>(mockDelegate, 0, 2, fakeTicker);

        cachingSubjectResolver.getTokenDetails(token);
        verify(mockDelegate, times(1)).getTokenDetails(token);

        cachingSubjectResolver.getTokenDetails(token);
        verify(mockDelegate, times(2)).getTokenDetails(token);
    }

}
