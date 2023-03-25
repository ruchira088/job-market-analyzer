package com.ruchij.api.dao.keyword.models;

import java.time.Instant;
import java.util.Optional;

public record Keyword(String id, String userId, Instant createdAt, String value, Optional<String> description) {
}