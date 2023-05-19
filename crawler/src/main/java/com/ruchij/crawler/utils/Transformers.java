package com.ruchij.crawler.utils;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Transformers {
	public static <T> CompletableFuture<T> convert(Optional<T> optionalValue, Supplier<Throwable> throwableSupplier) {
		return optionalValue.map(CompletableFuture::completedFuture)
			.orElseGet(() -> CompletableFuture.failedFuture(throwableSupplier.get()));
	}

	public static <T> CompletableFuture<T> lift(ThrowableSupplier<T> throwableSupplier) {
		try {
			return CompletableFuture.completedFuture(throwableSupplier.get());
		} catch (Exception exception) {
			return CompletableFuture.failedFuture(exception);
		}
	}

	public static <T> List<T> results(SearchResponse<T> searchResponse) {
		return searchResponse.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
	}

	public static <T> Optional<T> findFirst(SearchResponse<T> searchResponse) {
		return searchResponse.hits().hits().stream().map(Hit::source).findFirst();
	}

	public interface ThrowableSupplier<T> {
		T get() throws Exception;
	}
}
