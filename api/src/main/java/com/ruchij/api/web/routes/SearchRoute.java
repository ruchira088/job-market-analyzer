package com.ruchij.api.web.routes;

import com.ruchij.api.services.authorization.models.EntityType;
import com.ruchij.api.services.search.SearchService;
import com.ruchij.api.web.middleware.AuthenticationMiddleware;
import com.ruchij.api.web.middleware.AuthorizationMiddleware;
import com.ruchij.api.web.requests.Pagination;
import com.ruchij.api.web.responses.PaginatedResponse;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.HttpStatus;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class SearchRoute implements EndpointGroup {
	private final SearchService searchService;
	private final AuthenticationMiddleware authenticationMiddleware;
	private final AuthorizationMiddleware authorizationMiddleware;

	public SearchRoute(SearchService searchService, AuthenticationMiddleware authenticationMiddleware, AuthorizationMiddleware authorizationMiddleware) {
		this.searchService = searchService;
		this.authenticationMiddleware = authenticationMiddleware;
		this.authorizationMiddleware = authorizationMiddleware;
	}

	@Override
	public void addEndpoints() {
		path("jobs", () -> {
				path("crawler-task/<id>", () ->
					get(context -> {
							String crawlerTaskId = context.pathParamAsClass("id", String.class).get();
							Pagination pagination = Pagination.from(context);

							context.future(() ->
								this.authorizationMiddleware.hasPermission(context, EntityType.CRAWLER_TASK, crawlerTaskId)
									.thenCompose(__ ->
										this.searchService.findJobsByCrawlerTaskId(crawlerTaskId, pagination.pageSize(), pagination.pageNumber())
									)
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

		path("crawler-task", () -> {
			get(context -> {
				Pagination pagination = Pagination.from(context);

				context.future(() ->
					authenticationMiddleware.authenticate(context)
						.thenCompose(user ->
							searchService.findCrawlerTasksByUserId(user.userId(), pagination.pageSize(), pagination.pageNumber())
						)
						.thenApply(crawlerTasks ->
							context
								.status(HttpStatus.OK)
								.json(new PaginatedResponse<>(pagination.pageSize(), pagination.pageNumber(), crawlerTasks))
						)
				);
			});
		});
	}
}
