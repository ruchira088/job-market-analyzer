package com.ruchij.api.web.routes;

import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.requests.UserLoginRequest;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.delete;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthenticationRoute implements EndpointGroup {
    private final AuthenticationService authenticationService;
    private final AuthenticationMiddleware authenticationMiddleware;

    public AuthenticationRoute(
        AuthenticationService authenticationService,
        AuthenticationMiddleware authenticationMiddleware
    ) {
        this.authenticationService = authenticationService;
        this.authenticationMiddleware = authenticationMiddleware;
    }

    @Override
    public void addEndpoints() {
        post(context -> {
            UserLoginRequest userLoginRequest = context.bodyStreamAsClass(UserLoginRequest.class);

            context
                .status(HttpStatus.CREATED)
                .future(() -> authenticationService.login(userLoginRequest.email(), userLoginRequest.password()));
        });

        delete(context ->
            context
                .status(HttpStatus.OK)
                .future(() -> authenticationMiddleware.token(context).thenCompose(authenticationService::logout))
        );

    }
}
