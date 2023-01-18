package com.ruchij.api.services.health;

import com.ruchij.api.services.health.models.HealthCheck;
import com.ruchij.api.services.health.models.ServiceInformation;

import java.util.concurrent.CompletableFuture;

public interface HealthService {
    CompletableFuture<ServiceInformation> serviceInformation();

    CompletableFuture<HealthCheck> healthCheck();
}
