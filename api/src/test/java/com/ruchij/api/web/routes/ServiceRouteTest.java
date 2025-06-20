package com.ruchij.api.web.routes;

import com.ruchij.api.ApiApp;
import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.services.authorization.AuthorizationService;
import com.ruchij.api.services.crawler.ExtendedCrawlManager;
import com.ruchij.api.services.health.HealthService;
import com.ruchij.api.services.health.models.ServiceInformation;
import com.ruchij.api.services.search.SearchService;
import com.ruchij.api.services.user.UserService;
import com.ruchij.api.web.Routes;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsService;
import io.javalin.http.Header;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.ruchij.crawler.utils.JsonUtils.objectMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceRouteTest {

	@Test
	void shouldReturnServiceInformation() {
		HealthService healthService = Mockito.mock(HealthService.class);
		Instant instant = Instant.parse("2023-02-05T04:37:42.566735Z");

		Mockito.when(healthService.serviceInformation())
			.thenReturn(
				CompletableFuture.completedFuture(
					new ServiceInformation(
						"job-market-analyzer-api",
						"0.0.1-SNAPSHOT",
						"17.0.6",
						"7.6",
						instant,
						"main",
						"my-commit",
						instant
					)
				)
			);

		Routes routes =
			new Routes(
				Mockito.mock(ExtendedCrawlManager.class),
				Mockito.mock(UserService.class),
				Mockito.mock(AuthenticationService.class),
				Mockito.mock(AuthorizationService.class),
				Mockito.mock(LinkedInCredentialsService.class),
				Mockito.mock(SearchService.class),
				healthService
			);

		JavalinTest.test(
			ApiApp.httpApplication(routes, javalinConfig -> {
			}),
			(server, client) -> {
				try (Response response = client.request(
					"/service/info",
					builder ->
						builder
							.get()
							.addHeader(Header.ORIGIN, "http://localhost:3000")
				)) {

					assertEquals(200, response.code());
					assertEquals(List.of("http://localhost:3000"), response.headers().values(Header.ACCESS_CONTROL_ALLOW_ORIGIN));

					String expectedResponseBody = """
						{
							"serviceName": "job-market-analyzer-api",
							"serviceVersion": "0.0.1-SNAPSHOT",
							"javaVersion": "17.0.6",
							"gradleVersion":"7.6",
							"currentTimestamp": "2023-02-05T04:37:42.566735Z",
							"gitBranch": "main",
							"gitCommit": "my-commit",
							"buildTimestamp": "2023-02-05T04:37:42.566735Z"
						 }
						""";

					assertEquals(
						objectMapper.readTree(expectedResponseBody),
						objectMapper.readTree(response.body().byteStream())
					);
				}
			}
		);
	}
}