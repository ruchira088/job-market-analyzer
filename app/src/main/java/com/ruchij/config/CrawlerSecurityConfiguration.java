package com.ruchij.config;

import com.typesafe.config.Config;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public record CrawlerSecurityConfiguration(Key encryptionKey) {
    private static final String ALGORITHM = "AES";

    public static CrawlerSecurityConfiguration parse(Config config) {
        String base64EncryptionKey = config.getString("encryption-key");

        return create(base64EncryptionKey);
    }

    public static CrawlerSecurityConfiguration create(String base64EncryptionKey) {
        byte[] bytes = Base64.getDecoder().decode(base64EncryptionKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, ALGORITHM);

        return new CrawlerSecurityConfiguration(secretKeySpec);
    }
}
