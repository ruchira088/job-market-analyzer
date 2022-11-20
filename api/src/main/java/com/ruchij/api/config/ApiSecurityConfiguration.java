package com.ruchij.api.config;

import com.ruchij.config.CrawlerSecurityConfiguration;
import com.typesafe.config.Config;

import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.util.Base64;

public record ApiSecurityConfiguration(Key encryptionKey, IvParameterSpec defaultIV) {
    public static ApiSecurityConfiguration parse(Config config) {
        Key encryptionKey =
            CrawlerSecurityConfiguration.create(config.getString("encryption-key")).encryptionKey();

        IvParameterSpec ivParameterSpec =
            new IvParameterSpec(Base64.getDecoder().decode(config.getString("default-iv")));

        return new ApiSecurityConfiguration(encryptionKey, ivParameterSpec);
    }
}
