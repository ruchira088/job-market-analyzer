package com.ruchij.api.web.routes;

import com.ruchij.api.services.user.UserService;
import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.requests.CreateUserRequest;
import com.ruchij.api.web.responses.UserResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UserRoute implements EndpointGroup {
    private final UserService userService;
    private final AuthenticationMiddleware authenticationMiddleware;

    public UserRoute(UserService userService, AuthenticationMiddleware authenticationMiddleware) {
        this.userService = userService;
        this.authenticationMiddleware = authenticationMiddleware;
    }

    @Override
    public void addEndpoints() {
        post(context -> {
            CreateUserRequest createUserRequest = context.bodyStreamAsClass(CreateUserRequest.class);

            context
                .future(() ->
                    userService.create(
                            createUserRequest.getEmail(),
                            createUserRequest.getPassword(),
                            createUserRequest.getFirstName(),
                            createUserRequest.getLastName()
                        )
                        .thenApply(user ->
                            context
                                .status(HttpStatus.CREATED)
                                .json(UserResponse.from(user))
                        )
                );
        });

        path("authenticated", () ->
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
