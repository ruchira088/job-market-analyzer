package com.ruchij.api.config;

import java.security.Key;

public record ApiSecurityConfiguration(Key encryptionKey) {
}
