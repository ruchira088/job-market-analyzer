package com.ruchij.api.config;

import java.util.Optional;

public record RedisConfiguration(String host, int port, Optional<String> maybePassword) {
    public String uri() {
        return "redis://%s%s:%s"
            .formatted(
                maybePassword.map(password -> password + "@").orElse(""),
                host,
                port
            );
    }
}
