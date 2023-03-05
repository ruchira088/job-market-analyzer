package com.ruchij.api.config;

import com.typesafe.config.Config;

public record HttpConfiguration(String host, int port) {
	public static HttpConfiguration parse(Config config) {
		return new HttpConfiguration(
			config.getString("host"),
			config.getInt("port")
		);
	}
}
