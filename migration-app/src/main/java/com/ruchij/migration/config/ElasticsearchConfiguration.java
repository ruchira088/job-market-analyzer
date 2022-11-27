package com.ruchij.migration.config;

import com.typesafe.config.Config;

public record ElasticsearchConfiguration(String host, int port) {

    public static ElasticsearchConfiguration parse(Config config) {
        String host = config.getString("host");
        int port = config.getInt("port");

        return new ElasticsearchConfiguration(host, port);
    }
}
