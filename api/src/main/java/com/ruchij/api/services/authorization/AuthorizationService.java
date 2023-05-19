package com.ruchij.api.services.authorization;

import com.ruchij.api.services.authorization.models.EntityType;

import java.util.concurrent.CompletableFuture;

public interface AuthorizationService {
	CompletableFuture<Boolean> hasPermission(String userId, EntityType entityType, String entityId);
}
