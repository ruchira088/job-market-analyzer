package com.ruchij.crawler.dao.linkedin.models;

import com.ruchij.crawler.dao.elasticsearch.models.EncryptedText;

import java.time.Instant;

public record EncryptedLinkedInCredentials(
	String userId,
	Instant createdAt,
	EncryptedText email,
	EncryptedText password
) {
}
