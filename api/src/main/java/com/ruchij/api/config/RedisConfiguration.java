package com.ruchij.api.config;

import com.ruchij.migration.config.ConfigReaders;
import com.typesafe.config.Config;

import java.util.Optional;

public record RedisConfiguration(String host, int port, Optional<String> maybePassword) {
	public static RedisConfiguration parse(Config config) {
		return new RedisConfiguration(
			config.getString("host"),
			config.getInt("port"),
			ConfigReaders.optionalConfig(() -> config.getString("password"))
		);
	}

	public String uri() {
		return "redis://%s%s:%s"
			.formatted(
				maybePassword.map(password -> password + "@").orElse(""),
				host,
				port
			);
	}
}
