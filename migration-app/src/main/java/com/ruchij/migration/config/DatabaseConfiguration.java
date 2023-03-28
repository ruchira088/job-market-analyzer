package com.ruchij.migration.config;

import com.typesafe.config.Config;

public record DatabaseConfiguration(String url, String user, String password) {
	public static DatabaseConfiguration parse(Config config) {
		String url = config.getString("url");
		String user = config.getString("user");
		String password = config.getString("password");

		return new DatabaseConfiguration(url, user, password);
	}
}
