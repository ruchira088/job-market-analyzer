package com.ruchij.api.web.routes;

import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.requests.CreateLinkedInCredentialsRequest;
import com.ruchij.api.web.responses.SseType;
import com.ruchij.service.crawler.CrawlManager;
import com.ruchij.service.linkedin.LinkedInCredentialsService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static com.ruchij.utils.JsonUtils.objectMapper;
import static io.javalin.apibuilder.ApiBuilder.*;

public class LinkedInRoute implements EndpointGroup {
    private final LinkedInCredentialsService linkedInCredentialsService;
    private final CrawlManager crawlManager;
    private final AuthenticationMiddleware authenticationMiddleware;

    public LinkedInRoute(
        LinkedInCredentialsService linkedInCredentialsService,
        CrawlManager crawlManager,
        AuthenticationMiddleware authenticationMiddleware
    ) {
        this.linkedInCredentialsService = linkedInCredentialsService;
        this.crawlManager = crawlManager;
        this.authenticationMiddleware = authenticationMiddleware;
    }

    @Override
    public void addEndpoints() {
        path("credentials", () -> {
                post(context -> {
                    CreateLinkedInCredentialsRequest linkedInCredentialsRequest =
                        context.bodyStreamAsClass(CreateLinkedInCredentialsRequest.class);

                    context
                        .status(HttpStatus.CREATED)
                        .future(() ->
                            authenticationMiddleware.authenticate(context)
                                .thenCompose(user ->
                                    linkedInCredentialsService.insert(
                                        user.getUserId(),
                                        linkedInCredentialsRequest.getEmail(),
                                        linkedInCredentialsRequest.getPassword()
                                    )
                                )
                        );
                });

                get(context ->
                    context
                        .status(HttpStatus.OK)
                        .future(() ->
                            authenticationMiddleware.authenticate(context)
                                .thenCompose(user -> linkedInCredentialsService.getByUserId(user.getUserId()))
                        )
                );

                delete(context ->
                    context
                        .status(HttpStatus.OK)
                        .future(() ->
                            authenticationMiddleware.authenticate(context)
                                .thenCompose(user -> linkedInCredentialsService.deleteByUserId(user.getUserId()))
                        )
                );
            }
        );

        path("crawl", () ->
            sse(sseClient ->
                authenticationMiddleware.authenticate(sseClient.ctx())
                    .thenApply(user -> {
                            sseClient.sendEvent(SseType.CRAWL_STARTED.name(), null);

                            return crawlManager.run(user.getUserId())
                                .doFinally(() -> {
                                    sseClient.sendEvent(SseType.CRAWL_COMPLETED.name(), null);
                                    sseClient.close();
                                })
                                .subscribe(crawledJob ->
                                    sseClient.sendEvent(
                                        SseType.CRAWLED_JOB.name(),
                                        objectMapper.writeValueAsString(crawledJob))
                                );
                        }
                    ))
        );
    }
}
