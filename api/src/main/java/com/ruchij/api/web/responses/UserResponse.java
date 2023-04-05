package com.ruchij.api.web.responses;

import com.ruchij.api.dao.user.models.User;

import java.util.Optional;

public record UserResponse(String userId, String email, String firstName, Optional<String> lastName) {
	public static UserResponse from(User user) {
		return new UserResponse(user.id(), user.email(), user.firstName(), user.lastName());
	}
}
