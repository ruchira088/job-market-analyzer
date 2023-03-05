package com.ruchij.migration.config;

import com.typesafe.config.Config;

import java.util.Optional;

import static com.ruchij.migration.config.ConfigReaders.optionalConfig;

public record ElasticsearchConfiguration(String host, int port, Optional<Credentials> credentials) {

	public static ElasticsearchConfiguration parse(Config config) {
		String host = config.getString("host");
		int port = config.getInt("port");

		Optional<String> maybeUsername = optionalConfig(() -> config.getString("username"));
		Optional<String> maybePassword = optionalConfig(() -> config.getString("password"));

		Optional<Credentials> maybeElasticsearchCredentials =
			maybeUsername.flatMap(username ->
				maybePassword.map(password -> new Credentials(username, password))
			);


		return new ElasticsearchConfiguration(host, port, maybeElasticsearchCredentials);
	}

	public record Credentials(String username, String password) {
	}
}
