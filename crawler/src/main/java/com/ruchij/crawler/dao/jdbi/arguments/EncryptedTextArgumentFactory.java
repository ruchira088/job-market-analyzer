package com.ruchij.crawler.dao.jdbi.arguments;

import com.ruchij.crawler.dao.elasticsearch.models.EncryptedText;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

public class EncryptedTextArgumentFactory extends AbstractArgumentFactory<EncryptedText> {
	public EncryptedTextArgumentFactory() {
		super(Types.VARCHAR);
	}

	@Override
	protected Argument build(EncryptedText encryptedText, ConfigRegistry config) {
		return (position, preparedStatement, ctx) -> preparedStatement.setString(position, encryptedText.value());
	}
}