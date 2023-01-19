package com.ruchij.api.web.routes;

import com.ruchij.api.services.health.HealthService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class ServiceRoute implements EndpointGroup {
    private final HealthService healthService;

    public ServiceRoute(HealthService healthService) {
        this.healthService = healthService;
    }

    @Override
    public void addEndpoints() {
        path("health", () ->
            get(context ->
                context.future(() ->
                    healthService.healthCheck()
                        .thenAccept(healthCheck ->
                            context
                                .status(healthCheck.isHealthy() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                                .json(healthCheck)
                        )
                )
            )
        );
    }
}
