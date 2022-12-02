package com.ruchij.api.web.routes;

import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.requests.UserLoginRequest;
import com.ruchij.api.web.responses.AuthenticationTokenResponse;
import com.ruchij.api.web.responses.UserResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.*;

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
                .future(() ->
                    authenticationService.login(userLoginRequest.email(), userLoginRequest.password())
                        .thenApply(authenticationToken ->
                            context
                                .status(HttpStatus.OK)
                                .cookie(AuthenticationMiddleware.AUTHENTICATION_COOKIE, authenticationToken.token())
                                .json(AuthenticationTokenResponse.from(authenticationToken))
                        )
                );
        });

        delete(context ->
            context
                .future(() ->
                    authenticationMiddleware.token(context)
                        .thenCompose(authenticationService::logout)
                        .thenApply(authenticationToken ->
                            context
                                .status(HttpStatus.OK)
                                .json(AuthenticationTokenResponse.from(authenticationToken))
                        )
                )
        );

        path("user", () ->
            get(context ->
                context
                    .future(() ->
                        authenticationMiddleware.authenticate(context)
                            .thenApply(user ->
                                context
                                    .status(HttpStatus.OK)
                                    .json(UserResponse.from(user))
                            )
                    )
            )
        );
    }
}
