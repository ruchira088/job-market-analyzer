package com.ruchij.api.services.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruchij.api.dao.credentials.CredentialsDao;
import com.ruchij.api.dao.user.UserDao;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exceptions.AuthenticationException;
import com.ruchij.api.exceptions.ResourceNotFoundException;
import com.ruchij.api.kv.KeyValueStore;
import com.ruchij.api.services.authentication.models.AuthenticationToken;
import com.ruchij.api.services.hashing.PasswordHashingService;
import com.ruchij.api.utils.Transformers;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.random.RandomGenerator;
import com.ruchij.utils.JsonUtils;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final KeyValueStore keyValueStore;
    private final RandomGenerator<String> tokenGenerator;
    private final PasswordHashingService passwordHashingService;
    private final UserDao userDao;
    private final CredentialsDao credentialsDao;
    private final Clock clock;

    public AuthenticationServiceImpl(
        KeyValueStore keyValueStore,
        RandomGenerator<String> tokenGenerator,
        PasswordHashingService passwordHashingService,
        UserDao userDao,
        CredentialsDao credentialsDao,
        Clock clock
    ) {
        this.keyValueStore = keyValueStore;
        this.tokenGenerator = tokenGenerator;
        this.passwordHashingService = passwordHashingService;
        this.userDao = userDao;
        this.credentialsDao = credentialsDao;
        this.clock = clock;
    }

    @Override
    public CompletableFuture<AuthenticationToken> login(String email, String password) {
        return userDao.findByEmail(email)
            .thenCompose(optionalUser ->
                Transformers.convert(optionalUser, () -> new ResourceNotFoundException("User not found"))
            )
            .thenCompose(user ->
                credentialsDao.findByUserId(user.getUserId())
                    .thenCompose(optionalValue ->
                        Transformers.convert(
                            optionalValue,
                            () -> new ResourceNotFoundException("Credentials not found. userId=%s".formatted(user.getUserId()))
                        )
                    )
                    .thenCompose(credentials -> {
                        if (passwordHashingService.checkPassword(password, credentials.hashedPassword())) {
                            return CompletableFuture.completedFuture(user);
                        } else {
                            return CompletableFuture.failedFuture(new AuthenticationException("Password mismatch"));
                        }
                    })
            )
            .thenCompose(user -> {
                String token = tokenGenerator.generate();
                Instant timestamp = clock.timestamp();

                AuthenticationToken authenticationToken =
                    new AuthenticationToken(timestamp, user.getUserId(), token, null, 0);

                try {
                    String value = JsonUtils.objectMapper.writeValueAsString(authenticationToken);

                    return keyValueStore.put(token, value).thenApply(__ -> authenticationToken);
                } catch (JsonProcessingException jsonProcessingException) {
                    return CompletableFuture.failedFuture(jsonProcessingException);
                }
            });
    }

    @Override
    public CompletableFuture<User> authenticate(String token) {
        return keyValueStore.get(token)
            .thenCompose(optionalValue -> Transformers.convert(optionalValue, () -> new AuthenticationException("Token not found")))
            .thenCompose(valueString -> {
                try {
                    AuthenticationToken authenticationToken = JsonUtils.objectMapper.readValue(valueString, AuthenticationToken.class);

                    Instant timestamp = clock.timestamp();

                    if (authenticationToken.expiresAt().isAfter(timestamp)) {
                        String value =
                            JsonUtils.objectMapper.writeValueAsString(
                                authenticationToken.update(
                                    timestamp,
                                    authenticationToken.renewals() + 1
                                )
                            );

                        return keyValueStore.put(authenticationToken.token(), value)
                            .thenCompose(__ ->
                                userDao.findById(authenticationToken.userId())
                                    .thenCompose(optionalValue ->
                                        Transformers.convert(
                                            optionalValue,
                                            () -> new ResourceNotFoundException("User not found")
                                        )
                                    )
                            );
                    } else {
                        return CompletableFuture.failedFuture(new AuthenticationException("Token is expired"));
                    }
                } catch (JsonProcessingException jsonProcessingException) {
                    return CompletableFuture.failedFuture(jsonProcessingException);
                }
            });
    }
}
