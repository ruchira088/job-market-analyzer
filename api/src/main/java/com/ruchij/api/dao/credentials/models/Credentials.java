package com.ruchij.api.dao.credentials.models;

import java.util.Objects;

public class Credentials {
    private String userId;
    private String saltedHashedPassword;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSaltedHashedPassword() {
        return saltedHashedPassword;
    }

    public void setSaltedHashedPassword(String saltedHashedPassword) {
        this.saltedHashedPassword = saltedHashedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Credentials that = (Credentials) o;
        return Objects.equals(userId, that.userId) && Objects.equals(saltedHashedPassword, that.saltedHashedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, saltedHashedPassword);
    }
}
