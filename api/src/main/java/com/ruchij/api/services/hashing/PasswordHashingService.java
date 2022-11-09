package com.ruchij.api.services.hashing;

public interface PasswordHashingService {
    String hashPassword(String password);

    boolean checkPassword(String candidate, String hashedPassword);
}
