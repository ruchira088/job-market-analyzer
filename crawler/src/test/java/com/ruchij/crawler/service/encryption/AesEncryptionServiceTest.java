package com.ruchij.crawler.service.encryption;

import com.ruchij.crawler.config.CrawlerSecurityConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

class AesEncryptionServiceTest {
    private final String ENCRYPTION_KEY = "ahV2wB8G+6hQDwzfFoZcKlM4KC//qjA0Jq3TjewqgGQ=";

    private final CrawlerSecurityConfiguration crawlerSecurityConfiguration = CrawlerSecurityConfiguration.create(ENCRYPTION_KEY);
    private final AesEncryptionService aesEncryptionService =
        new AesEncryptionService(crawlerSecurityConfiguration.encryptionKey(), SecureRandom.getInstanceStrong());

    AesEncryptionServiceTest() throws NoSuchAlgorithmException {
    }

    @Test
    void performEncryptionAndDecryption() throws GeneralSecurityException {
        String data = "This is top secret";

        String encryptedBase64String = aesEncryptionService.encrypt(data.getBytes());
        Assertions.assertNotEquals(data, encryptedBase64String);

        byte[] decryptedBytes = aesEncryptionService.decrypt(encryptedBase64String);
        Assertions.assertEquals(data, new String(decryptedBytes));
    }

    @Test
    void performEncryptionAndDecryptionWithInitializationVector() throws GeneralSecurityException {
        String data = "This is top secret";
        String base64InitializationVector = "W6qURTo/i3zikcdrBpC/LQ==";

        IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(base64InitializationVector));

        String encryptedBase64String = aesEncryptionService.encrypt(data.getBytes(), iv);
        Assertions.assertEquals(
            "W6qURTo/i3zikcdrBpC/LQ==.ne2fCKHfGVESdM9Py4SaxO8qOCSkSOLdoVigK501HOY=",
            encryptedBase64String
        );

        byte[] decryptedBytes = aesEncryptionService.decrypt(encryptedBase64String);
        Assertions.assertEquals(data, new String(decryptedBytes));
    }

}