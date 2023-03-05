package com.ruchij.api.config;

import com.ruchij.crawler.config.CrawlerSecurityConfiguration;
import com.typesafe.config.Config;

import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.util.Base64;

public record ApiSecurityConfiguration(Key encryptionKey, IvParameterSpec defaultIV) {
	public static ApiSecurityConfiguration parse(Config config) {
		return create(config.getString("encryption-key"), config.getString("default-iv"));
	}

	public static ApiSecurityConfiguration create(String base64EncryptionKey, String base64Iv) {
		Key encryptionKey =
			CrawlerSecurityConfiguration.create(base64EncryptionKey).encryptionKey();

		IvParameterSpec ivParameterSpec =
			new IvParameterSpec(Base64.getDecoder().decode(base64Iv));

		return new ApiSecurityConfiguration(encryptionKey, ivParameterSpec);
	}
}
