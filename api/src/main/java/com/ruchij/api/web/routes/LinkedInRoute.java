package com.ruchij.api.web.routes;

import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.requests.CreateLinkedInCredentialsRequest;
import com.ruchij.service.linkedin.LinkedInCredentialsService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.post;

public class LinkedInRoute implements EndpointGroup {
    private final LinkedInCredentialsService linkedInCredentialsService;
    private final AuthenticationMiddleware authenticationMiddleware;

    public LinkedInRoute(
        LinkedInCredentialsService linkedInCredentialsService,
        AuthenticationMiddleware authenticationMiddleware
    ) {
        this.linkedInCredentialsService = linkedInCredentialsService;
        this.authenticationMiddleware = authenticationMiddleware;
    }

    @Override
    public void addEndpoints() {
        post(context -> {
            CreateLinkedInCredentialsRequest linkedInCredentialsRequest =
                context.bodyStreamAsClass(CreateLinkedInCredentialsRequest.class);

            context
                .status(HttpStatus.CREATED)
                .future(() ->
                    authenticationMiddleware.authenticate(context)
                        .thenCompose(user ->
                            linkedInCredentialsService.insert(
                                user.getUserId(),
                                linkedInCredentialsRequest.getEmail(),
                                linkedInCredentialsRequest.getPassword()
                            )
                        )
                );
        });
    }
}
