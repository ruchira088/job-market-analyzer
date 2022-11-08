package com.ruchij.service.encryption;

import com.ruchij.config.SecurityConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.GeneralSecurityException;

class AesEncryptionServiceTest {
    private final String ENCRYPTION_KEY = "ahV2wB8G+6hQDwzfFoZcKlM4KC//qjA0Jq3TjewqgGQ=";
    private final SecurityConfiguration securityConfiguration = SecurityConfiguration.create(ENCRYPTION_KEY);
    private final AesEncryptionService aesEncryptionService =
        new AesEncryptionService(securityConfiguration.encryptionKey());

    @Test
    void performEncryptionAndDecryption() throws GeneralSecurityException {
        String data = "This is top secret";

        String encryptedBase64String = aesEncryptionService.encrypt(data.getBytes());
        Assertions.assertNotEquals(data, encryptedBase64String);

        byte[] decryptedBytes = aesEncryptionService.decrypt(encryptedBase64String);
        Assertions.assertEquals(data, new String(decryptedBytes));
    }

}