package com.ruchij.api.services.user;

import com.ruchij.api.dao.user.models.User;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    CompletableFuture<User> create(String email, String password, String firstName, Optional<String> lastName);
}
