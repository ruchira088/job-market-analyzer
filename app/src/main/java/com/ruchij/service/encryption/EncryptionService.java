package com.ruchij.service.encryption;

import java.security.GeneralSecurityException;

public interface EncryptionService {
    String encrypt(byte[] input) throws GeneralSecurityException;

    byte[] decrypt(String encryptedData)throws GeneralSecurityException;
}
