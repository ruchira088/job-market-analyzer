package com.ruchij.api.services.lock.models;

import java.time.Instant;

public record Lock(String id, Instant acquiredAt, Instant expiresAt) {
}
