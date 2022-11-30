package com.ruchij.api.web.routes;

import com.ruchij.api.services.user.UserService;
import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.requests.CreateUserRequest;
import com.ruchij.api.web.responses.UserResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.post;

public class UserRoute implements EndpointGroup {
    private final UserService userService;

    public UserRoute(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void addEndpoints() {
        post(context -> {
            CreateUserRequest createUserRequest = context.bodyStreamAsClass(CreateUserRequest.class);

            context
                .future(() ->
                    userService.create(
                            createUserRequest.email(),
                            createUserRequest.password(),
                            createUserRequest.firstName(),
                            createUserRequest.lastName()
                        )
                        .thenApply(user ->
                            context
                                .status(HttpStatus.CREATED)
                                .json(UserResponse.from(user))
                        )
                );
        });
    }
}
