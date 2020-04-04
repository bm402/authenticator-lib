package com.bncrypted.authenticator.checker.spring.helper;

import com.bncrypted.authenticator.checker.core.SubjectResolver;
import com.bncrypted.authenticator.checker.core.model.User;
import com.bncrypted.authenticator.parser.core.user.exception.UserTokenInvalidException;

import java.util.concurrent.ConcurrentHashMap;

public class UserResolverHelper implements SubjectResolver<User> {

    private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public User getTokenDetails(String token) {
        User user = users.get(token);
        if (user == null) {
            throw new UserTokenInvalidException();
        }
        return user;
    }

    public void registerToken(String token, User user) {
        users.put(token, user);
    }

}
