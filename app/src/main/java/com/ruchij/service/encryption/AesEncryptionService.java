package com.ruchij.service.encryption;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.Base64;

import static com.ruchij.config.SecurityConfiguration.ALGORITHM;

public class AesEncryptionService implements EncryptionService {
    private final Key key;
    private final Base64.Encoder base64Encoder;
    private final Base64.Decoder base64Decoder;

    public AesEncryptionService(Key key) {
        this.key = key;
        this.base64Encoder = Base64.getEncoder();
        this.base64Decoder = Base64.getDecoder();
    }

    @Override
    public String encrypt(byte[] input) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(input);

        return new String(base64Encoder.encode(encryptedBytes));
    }

    @Override
    public byte[] decrypt(String encryptedData) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] encryptedBytes = base64Decoder.decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return decryptedBytes;
    }
}
