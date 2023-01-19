package com.ruchij.api.services.health.models;

import java.util.stream.Stream;

public record HealthCheck(HealthStatus elasticsearch, HealthStatus redis, HealthStatus internetConnectivity,
                          HealthStatus linkedInRendering) {
    public boolean isHealthy() {
        return Stream.of(elasticsearch, redis, internetConnectivity, linkedInRendering)
            .allMatch(healthStatus -> healthStatus == HealthStatus.HEALTHY);
    }
}
