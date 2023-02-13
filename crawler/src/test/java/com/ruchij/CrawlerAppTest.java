package com.ruchij;

import org.junit.jupiter.api.Test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

class CrawlerAppTest {
    @Test
    void appHasAGreeting() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);

        SecretKey secretKey = keyGenerator.generateKey();
        String key = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        System.out.println("Secret Key: %s".formatted(key));

        byte[] bytes = new byte[16];

        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);

        String iv = Base64.getEncoder().encodeToString(bytes);

        System.out.println("IV: %s".formatted(iv));
    }
}
