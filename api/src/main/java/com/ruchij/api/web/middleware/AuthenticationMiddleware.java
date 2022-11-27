package com.ruchij.api.web.middleware;

import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.exceptions.AuthenticationException;
import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.crawler.utils.Transformers;
import io.javalin.http.Context;
import io.javalin.http.Header;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AuthenticationMiddleware {
    public static final String TOKEN = "Bearer";
    private final AuthenticationService authenticationService;

    public AuthenticationMiddleware(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public CompletableFuture<String> token(Context context) {
        return Transformers.convert(
                Optional.ofNullable(context.header(Header.AUTHORIZATION)),
                () -> new AuthenticationException("Missing %s header".formatted(Header.AUTHORIZATION))
            )
            .thenCompose(authorizationHeader -> {
                if (authorizationHeader.toLowerCase().startsWith(TOKEN.toLowerCase())) {
                    return CompletableFuture.completedFuture(authorizationHeader.substring(TOKEN.length()).trim());
                } else {
                    return CompletableFuture.failedFuture(new AuthenticationException("Invalid authorization token type"));
                }
            });
    }

    public CompletableFuture<User> authenticate(Context context) {
        return token(context).thenCompose(authenticationService::authenticate);
    }

}
