package com.ruchij.api.utils;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Transformers {
    public static <T> CompletableFuture<T> convert(Optional<T> optionalValue, Supplier<Throwable> throwableSupplier) {
        return optionalValue.map(CompletableFuture::completedFuture)
            .orElseGet(() -> CompletableFuture.failedFuture(throwableSupplier.get()));
    }
}
