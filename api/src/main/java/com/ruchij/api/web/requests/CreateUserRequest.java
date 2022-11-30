package com.ruchij.api.web.requests;

import java.util.Optional;

public record CreateUserRequest(String email, String password, String firstName, Optional<String> lastName) {
}
