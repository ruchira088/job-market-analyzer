package com.ruchij.api.services.health.models;

public record HealthCheck(HealthStatus elasticsearch, HealthStatus redis, HealthStatus internetConnectivity,
                          HealthStatus linkedInRendering) {
}
