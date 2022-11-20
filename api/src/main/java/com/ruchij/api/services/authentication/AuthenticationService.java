package com.ruchij.api.services.authentication;

import com.ruchij.api.dao.user.models.User;
import com.ruchij.api.services.authentication.models.AuthenticationToken;

import java.util.concurrent.CompletableFuture;

public interface AuthenticationService {
    CompletableFuture<AuthenticationToken> login(String email, String password);

    CompletableFuture<User> authenticate(String token);

    CompletableFuture<AuthenticationToken> logout(String token);
}
