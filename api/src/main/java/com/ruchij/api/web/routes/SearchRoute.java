package com.ruchij.api.web.routes;

import com.ruchij.api.services.search.JobSearchService;
import com.ruchij.api.web.responses.PaginatedResponse;
import com.ruchij.crawler.dao.job.models.Job;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import java.util.Optional;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class SearchRoute implements EndpointGroup {
    private final JobSearchService jobSearchService;

    public SearchRoute(JobSearchService jobSearchService) {
        this.jobSearchService = jobSearchService;
    }

    @Override
    public void addEndpoints() {
        path("crawler-id/<crawlerId>", () ->
            get(context -> {
                    String crawlerId = context.pathParamAsClass("crawlerId", String.class).get();
                    Integer pageSize =
                        Optional.ofNullable(context.queryParamAsClass("page-size", Integer.class).allowNullable().get())
                            .orElse(20);
                    Integer pageNumber =
                        Optional.ofNullable(context.queryParamAsClass("page-number", Integer.class).allowNullable().get())
                            .orElse(0);

                    context.future(() ->
                        jobSearchService.findByCrawlerId(crawlerId, pageSize, pageNumber)
                            .thenApply(jobs ->
                                context
                                    .status(HttpStatus.OK)
                                    .json(new PaginatedResponse<>(pageSize, pageNumber, jobs))
                            )
                    );
                }
            )
        );
    }
}
