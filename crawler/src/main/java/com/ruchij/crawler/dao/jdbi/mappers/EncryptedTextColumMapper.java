package com.ruchij.crawler.dao.jdbi.mappers;

import com.ruchij.crawler.dao.elasticsearch.models.EncryptedText;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EncryptedTextColumMapper implements ColumnMapper<EncryptedText> {

	@Override
	public EncryptedText map(ResultSet resultSet, int columnNumber, StatementContext ctx) throws SQLException {
		return new EncryptedText(resultSet.getString(columnNumber));
	}
}