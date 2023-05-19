package com.ruchij.api.web.middleware;

import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exceptions.AuthorizationException;
import com.ruchij.api.services.authorization.AuthorizationService;
import com.ruchij.api.services.authorization.models.EntityType;
import io.javalin.http.Context;

import java.util.concurrent.CompletableFuture;

public class AuthorizationMiddleware {
	private final AuthenticationMiddleware authenticationMiddleware;
	private final AuthorizationService authorizationService;

	public AuthorizationMiddleware(AuthenticationMiddleware authenticationMiddleware, AuthorizationService authorizationService) {
		this.authenticationMiddleware = authenticationMiddleware;
		this.authorizationService = authorizationService;
	}

	public CompletableFuture<User> hasPermission(Context context, EntityType entityType, String entityId) {
		return this.authenticationMiddleware.authenticate(context)
			.thenCompose(user ->
				this.authorizationService.hasPermission(user.id(), entityType, entityId)
					.thenCompose(hasPermission ->
						hasPermission ?
							CompletableFuture.completedFuture(user) :
							CompletableFuture.failedFuture(
								new AuthorizationException(
									"%s does not have authorization to access %s (id=%s)"
										.formatted(user.id(), entityType, entityId)
								)
							)
					)
			);
	}
}
