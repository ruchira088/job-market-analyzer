package com.ruchij.api.services.authentication;

import com.ruchij.api.dao.user.models.User;

import java.util.concurrent.CompletableFuture;

public interface AuthenticationService {
    CompletableFuture<String> login(String email, String password);

    CompletableFuture<User> authenticate(String token);
}
