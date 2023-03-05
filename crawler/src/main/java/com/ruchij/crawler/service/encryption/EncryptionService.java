package com.ruchij.crawler.service.encryption;

import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;

public interface EncryptionService {
	String encrypt(byte[] input) throws GeneralSecurityException;

	String encrypt(byte[] input, IvParameterSpec iv) throws GeneralSecurityException;

	byte[] decrypt(String encryptedData) throws GeneralSecurityException;
}
