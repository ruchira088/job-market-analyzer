package com.ruchij.api.services.authentication.models;

import java.time.Instant;

public record AuthenticationToken(Instant issuedAt, String userId, String token, Instant expiresAt, long renewals) {
    public AuthenticationToken update(Instant expiresAt, long renewals) {
        return new AuthenticationToken(issuedAt, userId, token, expiresAt, renewals);
    }
}
