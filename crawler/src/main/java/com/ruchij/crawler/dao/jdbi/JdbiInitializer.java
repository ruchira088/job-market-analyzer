package com.ruchij.crawler.dao.jdbi;

import com.ruchij.crawler.dao.jdbi.arguments.EncryptedTextArgumentFactory;
import com.ruchij.crawler.dao.jdbi.mappers.EncryptedTextColumMapper;
import org.jdbi.v3.core.Jdbi;

public class JdbiInitializer {
	public static void initialize(Jdbi jdbi) {
		jdbi.registerColumnMapper(new EncryptedTextColumMapper());
		jdbi.registerArgument(new EncryptedTextArgumentFactory());
	}
}
