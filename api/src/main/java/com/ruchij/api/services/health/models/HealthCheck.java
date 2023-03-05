package com.ruchij.api.services.health.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.stream.Stream;

public record HealthCheck(HealthStatus elasticsearch,
                          HealthStatus redis,
                          HealthStatus internetConnectivity,
                          HealthStatus linkedInRendering) {
	@JsonIgnore
	public boolean isHealthy() {
		return Stream.of(elasticsearch, redis, internetConnectivity, linkedInRendering)
			.allMatch(healthStatus -> healthStatus == HealthStatus.HEALTHY);
	}
}
