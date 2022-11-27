package com.ruchij.crawler.utils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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

    public interface ThrowableSupplier<T> {
        T get() throws Exception;
    }
}
