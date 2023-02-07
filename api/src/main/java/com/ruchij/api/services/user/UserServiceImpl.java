package com.ruchij.api.services.user;

import com.ruchij.api.dao.credentials.CredentialsDao;
import com.ruchij.api.dao.credentials.models.Credentials;
import com.ruchij.api.dao.user.UserDao;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exceptions.ResourceConflictException;
import com.ruchij.api.services.hashing.PasswordHashingService;
import com.ruchij.crawler.service.random.RandomGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;
    private final CredentialsDao credentialsDao;
    private final PasswordHashingService passwordHashingService;
    private final RandomGenerator<String> idGenerator;
    private final Clock clock;

    public UserServiceImpl(
        UserDao userDao,
        CredentialsDao credentialsDao,
        PasswordHashingService passwordHashingService,
        RandomGenerator<String> idGenerator,
        Clock clock
    ) {
        this.userDao = userDao;
        this.credentialsDao = credentialsDao;
        this.passwordHashingService = passwordHashingService;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public CompletableFuture<User> create(String email, String password, String firstName, Optional<String> lastName) {
        return userDao.findByEmail(email)
            .thenCompose(maybeUser -> {
                if (maybeUser.isPresent()) {
                    return CompletableFuture.failedFuture(
                        new ResourceConflictException("User with email=%s already exists".formatted(email))
                    );
                } else {
                    return CompletableFuture.completedFuture(null);
                }
            })
            .thenCompose(__ -> {
                String userId = idGenerator.generate();
                Instant timestamp = clock.instant();

                User user =
                    new User(userId, timestamp, email, firstName, lastName);

                String hashedPassword = passwordHashingService.hashPassword(password);

                Credentials credentials = new Credentials(userId, hashedPassword);

                return userDao.insert(user)
                    .thenCompose(___ -> credentialsDao.insert(credentials))
                    .thenApply(___ -> user);
            });
    }
}
