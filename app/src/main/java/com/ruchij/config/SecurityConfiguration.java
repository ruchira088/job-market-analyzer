package com.ruchij.config;

import com.typesafe.config.Config;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public record SecurityConfiguration(Key encryptionKey) {
    public static final String ALGORITHM = "AES";

    public static SecurityConfiguration parse(Config config) {
        String base64EncryptionKey = config.getString("encryption-key");

        return create(base64EncryptionKey);
    }

    public static SecurityConfiguration create(String base64EncryptionKey) {
        byte[] bytes = Base64.getDecoder().decode(base64EncryptionKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, ALGORITHM);

        return new SecurityConfiguration(secretKeySpec);
    }
}
