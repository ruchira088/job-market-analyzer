package com.ruchij.crawler.service.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;

public class AesEncryptionService implements EncryptionService {
	private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final String SEPARATOR = ".";

	private final Key key;
	private final SecureRandom secureRandom;
	private final Base64.Encoder base64Encoder;
	private final Base64.Decoder base64Decoder;

	public AesEncryptionService(Key key, SecureRandom secureRandom) {
		this.key = key;
		this.secureRandom = secureRandom;
		this.base64Encoder = Base64.getEncoder();
		this.base64Decoder = Base64.getDecoder();
	}

	@Override
	public String encrypt(byte[] input) throws GeneralSecurityException {
		byte[] bytes = new byte[16];
		secureRandom.nextBytes(bytes);

		return encrypt(input, new IvParameterSpec(bytes));
	}

	@Override
	public String encrypt(byte[] input, IvParameterSpec iv) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] encryptedBytes = cipher.doFinal(input);

		return base64Encoder.encodeToString(iv.getIV()) + SEPARATOR + base64Encoder.encodeToString(encryptedBytes);
	}

	@Override
	public byte[] decrypt(String input) throws GeneralSecurityException {
		int separatorIndex = input.indexOf(SEPARATOR);

		if (separatorIndex < 0) {
			throw new GeneralSecurityException("Invalid encrypted data");
		}

		String ivBase64String = input.substring(0, separatorIndex);
		String encryptedDataBase64String = input.substring(separatorIndex + 1);

		IvParameterSpec iv = new IvParameterSpec(base64Decoder.decode(ivBase64String));
		byte[] encryptedBytes = base64Decoder.decode(encryptedDataBase64String);

		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key, iv);

		byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

		return decryptedBytes;
	}
}
