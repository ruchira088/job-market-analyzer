package com.ruchij.dao.linkedin.models;

import com.ruchij.dao.elasticsearch.models.EncryptedText;

import java.time.Instant;

public class EncryptedLinkedInCredentials {
    private String userId;
    private Instant createdAt;
    private EncryptedText email;
    private EncryptedText password;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public EncryptedText getEmail() {
        return email;
    }

    public void setEmail(EncryptedText email) {
        this.email = email;
    }

    public EncryptedText getPassword() {
        return password;
    }

    public void setPassword(EncryptedText password) {
        this.password = password;
    }
}
