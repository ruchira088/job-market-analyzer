package com.ruchij.api.dao.lock.models;

import java.time.Instant;
import java.util.Objects;

public final class DatabaseLock {
	private String id;
	private Instant acquiredAt;
	private Instant expiresAt;

	public DatabaseLock(String id, Instant acquiredAt, Instant expiresAt) {
		this.id = id;
		this.acquiredAt = acquiredAt;
		this.expiresAt = expiresAt;
	}

	public DatabaseLock() {}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Instant getAcquiredAt() {
		return acquiredAt;
	}

	public void setAcquiredAt(Instant acquiredAt) {
		this.acquiredAt = acquiredAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(Instant expiresAt) {
		this.expiresAt = expiresAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		DatabaseLock that = (DatabaseLock) o;
		return Objects.equals(id, that.id) && Objects.equals(acquiredAt, that.acquiredAt) && Objects.equals(expiresAt, that.expiresAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, acquiredAt, expiresAt);
	}

	@Override
	public String toString() {
		return "DatabaseLock{" +
			"id='" + id + '\'' +
			", acquiredAt=" + acquiredAt +
			", expiresAt=" + expiresAt +
			'}';
	}
}