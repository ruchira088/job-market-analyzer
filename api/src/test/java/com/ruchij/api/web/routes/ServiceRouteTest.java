package com.ruchij.api.web.routes;

import com.ruchij.api.ApiApp;
import com.ruchij.api.services.authentication.AuthenticationService;
import com.ruchij.api.services.crawler.ExtendedCrawlManager;
import com.ruchij.api.services.health.HealthService;
import com.ruchij.api.services.health.models.ServiceInformation;
import com.ruchij.api.services.search.JobSearchService;
import com.ruchij.api.services.user.UserService;
import com.ruchij.api.web.Routes;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsService;
import io.javalin.testtools.JavalinTest;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
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
                Mockito.mock(LinkedInCredentialsService.class),
                Mockito.mock(JobSearchService.class),
                healthService
            );

        JavalinTest.test(
            ApiApp.httpApplication(routes, javalinConfig -> {}),
            (server, client) -> {
                Response response = client.get("/service/info");
                assertEquals(200, response.code());

                String expectedResponseBody = """
                {
                    "serviceName":"job-market-analyzer-api",
                    "serviceVersion":"0.0.1-SNAPSHOT",
                    "javaVersion":"17.0.6",
                    "gradleVersion":"7.6",
                    "currentTimestamp":1675571862.566735000,
                    "gitBranch":"main",
                    "gitCommit":"my-commit",
                    "buildTimestamp":1675571862.566735000
                 }
                """;

                assertEquals(
                    objectMapper.readTree(expectedResponseBody),
                    objectMapper.readTree(response.body().byteStream())
                );
            }
        );
    }
}