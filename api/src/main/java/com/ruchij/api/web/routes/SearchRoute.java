package com.ruchij.api.web.routes;

import com.ruchij.api.services.search.JobSearchService;
import com.ruchij.api.web.requests.Pagination;
import com.ruchij.api.web.responses.PaginatedResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class SearchRoute implements EndpointGroup {
	private final JobSearchService jobSearchService;

	public SearchRoute(JobSearchService jobSearchService) {
		this.jobSearchService = jobSearchService;
	}

	@Override
	public void addEndpoints() {
		path("jobs", () -> {
				path("crawler-task/<id>", () ->
					get(context -> {
							String crawlerTaskId = context.pathParamAsClass("id", String.class).get();
							Pagination pagination = Pagination.from(context);

							context.future(() ->
								jobSearchService.findByCrawlerTaskId(crawlerTaskId, pagination.pageSize(), pagination.pageNumber())
									.thenApply(jobs ->
										context
											.status(HttpStatus.OK)
											.json(new PaginatedResponse<>(pagination.pageSize(), pagination.pageNumber(), jobs))
									)
							);
						}
					)
				);
			}
		);
	}
}
