package com.ruchij.api.web.requests;

import io.javalin.http.Context;

import java.util.Optional;

public record Pagination(int pageSize, int pageNumber) {
	public static Pagination from(Context context) {
		Integer pageSize =
			Optional.ofNullable(context.queryParamAsClass("page-size", Integer.class).allowNullable().get())
				.orElse(20);

		Integer pageNumber =
			Optional.ofNullable(context.queryParamAsClass("page-number", Integer.class).allowNullable().get())
				.orElse(0);

		return new Pagination(pageSize, pageNumber);
	}
}
