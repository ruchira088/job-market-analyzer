package com.ruchij.api.dao.user.models;

import java.time.Instant;
import java.util.Optional;

public record User(String id, Instant createdAt, String email, String firstName, Optional<String> lastName) {
}
