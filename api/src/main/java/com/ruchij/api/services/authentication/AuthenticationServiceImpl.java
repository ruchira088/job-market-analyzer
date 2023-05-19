package com.ruchij.api.services.authentication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruchij.api.dao.credentials.CredentialsDao;
import com.ruchij.api.dao.user.UserDao;
import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exceptions.AuthenticationException;
import com.ruchij.api.kv.KeyValueStore;
import com.ruchij.api.services.authentication.models.AuthenticationToken;
import com.ruchij.api.services.hashing.PasswordHashingService;
import com.ruchij.crawler.dao.transaction.Transactor;
import com.ruchij.crawler.exceptions.ResourceNotFoundException;
import com.ruchij.crawler.service.random.RandomGenerator;
import com.ruchij.crawler.utils.JsonUtils;
import com.ruchij.crawler.utils.Transformers;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class AuthenticationServiceImpl<A> implements AuthenticationService {
	private static final Duration SESSION_DURATION = Duration.ofDays(7);

	private final KeyValueStore keyValueStore;
	private final RandomGenerator<String> tokenGenerator;
	private final PasswordHashingService passwordHashingService;
	private final UserDao<A> userDao;
	private final CredentialsDao<A> credentialsDao;
	private final Transactor<A> transactor;
	private final Clock clock;

	public AuthenticationServiceImpl(
		KeyValueStore keyValueStore,
		RandomGenerator<String> tokenGenerator,
		PasswordHashingService passwordHashingService,
		UserDao<A> userDao,
		CredentialsDao<A> credentialsDao,
		Transactor<A> transactor,
		Clock clock
	) {
		this.keyValueStore = keyValueStore;
		this.tokenGenerator = tokenGenerator;
		this.passwordHashingService = passwordHashingService;
		this.userDao = userDao;
		this.credentialsDao = credentialsDao;
		this.transactor = transactor;
		this.clock = clock;
	}

	@Override
	public CompletableFuture<AuthenticationToken> login(String email, String password) {
		return transactor.transaction(
				userDao.findByEmail(email)
					.semiFlatMap(optionalUser ->
						Transformers.convert(optionalUser, () -> new ResourceNotFoundException("User not found"))
					)
					.flatMap(user ->
						credentialsDao.findByUserId(user.id())
							.semiFlatMap(optionalValue ->
								Transformers.convert(
									optionalValue,
									() -> new ResourceNotFoundException("Credentials not found. id=%s".formatted(user.id()))
								)
							)
							.semiFlatMap(credentials -> {
								if (passwordHashingService.checkPassword(password, credentials.hashedPassword())) {
									return CompletableFuture.completedFuture(user);
								} else {
									return CompletableFuture.failedFuture(new AuthenticationException("Password mismatch"));
								}
							})
					)
			)
			.thenCompose(user -> {
				String token = tokenGenerator.generate();
				Instant timestamp = clock.instant();

				AuthenticationToken authenticationToken =
					new AuthenticationToken(timestamp, user.id(), token, timestamp.plus(SESSION_DURATION), 0);

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
		return existingToken(token)
			.thenCompose(authenticationToken -> {
				Instant timestamp = clock.instant();

				if (authenticationToken.expiresAt().isAfter(timestamp)) {
					try {
						String value =
							JsonUtils.objectMapper.writeValueAsString(
								authenticationToken.update(
									timestamp.plus(SESSION_DURATION),
									authenticationToken.renewals() + 1
								)
							);

						return keyValueStore.put(authenticationToken.token(), value)
							.thenCompose(__ ->
								transactor.transaction(userDao.findById(authenticationToken.userId()))
									.thenCompose(optionalValue ->
										Transformers.convert(
											optionalValue,
											() -> new ResourceNotFoundException("User not found")
										)
									)
							);
					} catch (JsonProcessingException jsonProcessingException) {
						return CompletableFuture.failedFuture(jsonProcessingException);
					}
				} else {
					return CompletableFuture.failedFuture(new AuthenticationException("Token is expired"));
				}
			});
	}

	@Override
	public CompletableFuture<AuthenticationToken> logout(String token) {
		return existingToken(token)
			.thenCompose(authenticationToken -> keyValueStore.delete(token).thenApply(__ -> authenticationToken));
	}

	private CompletableFuture<AuthenticationToken> existingToken(String token) {
		return keyValueStore.get(token)
			.thenCompose(optionalValue -> Transformers.convert(optionalValue, () -> new AuthenticationException("Token not found")))
			.thenCompose(valueString -> {
				try {
					AuthenticationToken authenticationToken =
						JsonUtils.objectMapper.readValue(valueString, AuthenticationToken.class);

					return CompletableFuture.completedFuture(authenticationToken);
				} catch (JsonProcessingException jsonProcessingException) {
					return CompletableFuture.failedFuture(jsonProcessingException);
				}
			});
	}
}
