package com.ruchij.development;

import com.ruchij.api.ApiApp;
import com.ruchij.api.config.ApiConfiguration;
import com.ruchij.api.config.ApiSecurityConfiguration;
import com.ruchij.api.config.HttpConfiguration;
import com.ruchij.api.config.RedisConfiguration;
import com.ruchij.api.web.Routes;
import com.ruchij.development.providers.ConfigurationProvider;
import com.ruchij.development.providers.ContainerConfigurationProvider;
import com.ruchij.development.providers.DockerComposeConfigurationProvider;
import com.ruchij.migration.config.DatabaseConfiguration;
import com.ruchij.migration.config.ElasticsearchConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevelopmentApp {
	private static final String ENCRYPTION_KEY = "ahV2wB8G+6hQDwzfFoZcKlM4KC//qjA0Jq3TjewqgGQ=";
	private static final String DEFAULT_IV = "W6qURTo/i3zikcdrBpC/LQ==";

	private static final Logger logger = LoggerFactory.getLogger(DevelopmentApp.class);

	public static void main(String[] args) throws Exception {
//        ConfigurationProvider configurationProvider = new ContainerConfigurationProvider();
		ConfigurationProvider configurationProvider = new DockerComposeConfigurationProvider();

		ElasticsearchConfiguration elasticsearchConfiguration = configurationProvider.elasticsearchConfiguration();

		logger.info("Elasticsearch is ready");

		DatabaseConfiguration databaseConfiguration = configurationProvider.databaseConfiguration();

		logger.info("Database is ready");

		RedisConfiguration redisConfiguration = configurationProvider.redisConfiguration();

		logger.info("Redis is ready");

		ApiSecurityConfiguration apiSecurityConfiguration =
			ApiSecurityConfiguration.create(ENCRYPTION_KEY, DEFAULT_IV);

		HttpConfiguration httpConfiguration = new HttpConfiguration("0.0.0.0", 443);

		ApiConfiguration apiConfiguration =
			new ApiConfiguration(
				elasticsearchConfiguration,
				databaseConfiguration,
				redisConfiguration,
				apiSecurityConfiguration,
				httpConfiguration
			);

		Routes routes = ApiApp.routes(apiConfiguration);

		ApiApp.httpApplication(
				routes,
				javalinConfig -> javalinConfig.jetty.server(() -> jettyServer(httpConfiguration))
			)
			.start();

		logger.info("HTTPS API is ready on port %s".formatted(httpConfiguration.port()));
	}

	private static Server jettyServer(HttpConfiguration httpConfiguration) {
		Server server = new Server();

		SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
		sslContextFactory.setKeyStorePath(DevelopmentApp.class.getResource("/localhost.jks").toExternalForm());
		sslContextFactory.setKeyStorePassword("changeit");

		ServerConnector sslServerConnector = new ServerConnector(server, sslContextFactory);
		sslServerConnector.setPort(httpConfiguration.port());

		server.setConnectors(new Connector[]{sslServerConnector});

		return server;
	}
}
