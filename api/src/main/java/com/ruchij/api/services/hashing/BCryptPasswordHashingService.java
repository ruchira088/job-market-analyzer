package com.ruchij.api.services.hashing;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordHashingService implements PasswordHashingService {
    @Override
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public boolean checkPassword(String candidate, String hashedPassword) {
        return BCrypt.checkpw(candidate, hashedPassword);
    }
}
