package com.ruchij.api.web.routes;

import com.ruchij.api.services.crawler.ExtendedCrawlManager;
import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.requests.CreateLinkedInCredentialsRequest;
import com.ruchij.api.web.responses.ErrorResponse;
import com.ruchij.api.web.responses.SseType;
import com.ruchij.crawler.service.linkedin.LinkedInCredentialsService;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static com.ruchij.crawler.utils.JsonUtils.objectMapper;
import static io.javalin.apibuilder.ApiBuilder.*;

public class LinkedInRoute implements EndpointGroup {
    private final LinkedInCredentialsService linkedInCredentialsService;
    private final ExtendedCrawlManager extendedCrawlManager;
    private final AuthenticationMiddleware authenticationMiddleware;

    public LinkedInRoute(
        LinkedInCredentialsService linkedInCredentialsService,
        ExtendedCrawlManager extendedCrawlManager,
        AuthenticationMiddleware authenticationMiddleware
    ) {
        this.linkedInCredentialsService = linkedInCredentialsService;
        this.extendedCrawlManager = extendedCrawlManager;
        this.authenticationMiddleware = authenticationMiddleware;
    }

    @Override
    public void addEndpoints() {
        path("credentials", () -> {
                post(context -> {
                    CreateLinkedInCredentialsRequest linkedInCredentialsRequest =
                        context.bodyStreamAsClass(CreateLinkedInCredentialsRequest.class);

                    context
                        .future(() ->
                            authenticationMiddleware.authenticate(context)
                                .thenCompose(user ->
                                    linkedInCredentialsService.insert(
                                        user.userId(),
                                        linkedInCredentialsRequest.email(),
                                        linkedInCredentialsRequest.password()
                                    )
                                )
                                .thenAccept(linkedInCredentials ->
                                    context
                                        .status(HttpStatus.CREATED)
                                        .json(linkedInCredentials)
                                )
                        );
                });

                get(context ->
                    context
                        .future(() ->
                            authenticationMiddleware.authenticate(context)
                                .thenCompose(user -> linkedInCredentialsService.getByUserId(user.userId()))
                                .thenAccept(linkedInCredentials ->
                                    context
                                        .status(HttpStatus.OK)
                                        .json(linkedInCredentials)
                                )
                        )
                );

                delete(context ->
                    context
                        .future(() ->
                            authenticationMiddleware.authenticate(context)
                                .thenCompose(user -> linkedInCredentialsService.deleteByUserId(user.userId()))
                                .thenAccept(linkedInCredentials ->
                                    context
                                        .status(HttpStatus.OK)
                                        .json(linkedInCredentials)
                                )
                        )
                );
            }
        );

        path("crawl", () ->
            sse(sseClient -> {
                sseClient.keepAlive();

                authenticationMiddleware.authenticate(sseClient.ctx())
                    .thenApply(user -> {
                            sseClient.sendEvent(SseType.CRAWL_STARTED.name(), "{}");

                            return extendedCrawlManager.runWithLock(user.userId())
                                .doFinally(() -> {
                                    if (!sseClient.terminated()) {
                                        sseClient.sendEvent(SseType.CRAWL_COMPLETED.name(), "{}");
                                        sseClient.close();
                                    }
                                })
                                .doOnError(throwable -> {
                                    sseClient.sendEvent(
                                        SseType.CRAWL_ERROR.name(),
                                        objectMapper.writeValueAsString(new ErrorResponse(throwable.getMessage()))
                                    );
                                    sseClient.close();
                                })
                                .subscribe(crawledJob -> {
                                        if (!sseClient.terminated()) {
                                            sseClient.sendEvent(
                                                SseType.CRAWLED_JOB.name(),
                                                objectMapper.writeValueAsString(crawledJob)
                                            );
                                        }
                                    }
                                );
                        }
                    )
                    .thenAccept(disposable ->
                        sseClient.onClose(() -> {
                            if (!disposable.isDisposed()) {
                                disposable.dispose();
                            }
                        })
                    );
            })
        );
    }
}
