package com.ruchij.api.services.authentication;

import com.ruchij.api.dao.user.UserDao;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.kv.KeyValueStore;
import com.ruchij.api.services.hashing.PasswordHashingService;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.random.RandomGenerator;

import java.util.concurrent.CompletableFuture;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final KeyValueStore keyValueStore;
    private final RandomGenerator<String> tokenGenerator;
    private final PasswordHashingService passwordHashingService;
    private final UserDao userDao;
    private final Clock clock;

    public AuthenticationServiceImpl(
        KeyValueStore keyValueStore,
        RandomGenerator<String> tokenGenerator,
        PasswordHashingService passwordHashingService,
        UserDao userDao,
        Clock clock
    ) {
        this.keyValueStore = keyValueStore;
        this.tokenGenerator = tokenGenerator;
        this.passwordHashingService = passwordHashingService;
        this.userDao = userDao;
        this.clock = clock;
    }

    @Override
    public CompletableFuture<String> login(String email, String password) {
        return null;
    }

    @Override
    public CompletableFuture<User> authenticate(String token) {
        return null;
    }
}
