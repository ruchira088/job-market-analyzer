package com.ruchij.api.dao.jdbi.models;

import com.ruchij.api.dao.user.models.User;

import java.time.Instant;
import java.util.Optional;

public class JdbiUser {
	private String id;
	private Instant createdAt;
	private String email;
	private String firstName;
	private String lastName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public User user() {
		return new User(id, createdAt, email, firstName, Optional.ofNullable(lastName));
	}

	public static JdbiUser from(User user) {
		JdbiUser jdbiUser = new JdbiUser();
		jdbiUser.setId(user.id());
		jdbiUser.setCreatedAt(user.createdAt());
		jdbiUser.setEmail(user.email());
		jdbiUser.setFirstName(user.firstName());
		jdbiUser.setLastName(user.lastName().orElse(null));

		return jdbiUser;
	}
}