package com.ruchij.api.web.routes;

import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.web.requests.UserLoginRequest;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthenticationRoute implements EndpointGroup {
    private final AuthenticationService authenticationService;

    public AuthenticationRoute(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void addEndpoints() {
        post(context -> {
            UserLoginRequest userLoginRequest = context.bodyStreamAsClass(UserLoginRequest.class);

            context.status(HttpStatus.CREATED)
                .future(() -> authenticationService.login(userLoginRequest.email(), userLoginRequest.password()));
        });

    }
}
