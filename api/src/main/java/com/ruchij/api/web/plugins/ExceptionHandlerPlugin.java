package com.ruchij.api.web.plugins;

import com.ruchij.api.exceptions.AuthenticationException;
import com.ruchij.api.exceptions.ResourceConflictException;
import com.ruchij.api.web.responses.ErrorResponse;
import com.ruchij.crawler.exceptions.ResourceNotFoundException;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ExceptionHandlerPlugin implements Plugin {
    private Map<Class<? extends Exception>, HttpStatus> errorCodes() {
        Map<Class<? extends Exception>, HttpStatus> errorMappings = new HashMap<>();

        errorMappings.put(ResourceConflictException.class, HttpStatus.CONFLICT);
        errorMappings.put(AuthenticationException.class, HttpStatus.UNAUTHORIZED);
        errorMappings.put(ResourceNotFoundException.class, HttpStatus.NOT_FOUND);

        return errorMappings;
    }

    private Throwable rootCause(Throwable throwable) {
        return Optional.ofNullable(throwable.getCause()).map(this::rootCause).orElse(throwable);
    }

    @Override
    public void apply(@NotNull Javalin javalin) {
        for (Map.Entry<Class<? extends Exception>, HttpStatus> entry : errorCodes().entrySet()) {
            javalin.exception(
                entry.getKey(),
                (exception, context) ->
                    context
                        .status(entry.getValue())
                        .json(new ErrorResponse(
                                Optional.ofNullable(rootCause(exception).getMessage())
                                    .orElse(entry.getKey().getCanonicalName())
                            )
                        )
            );
        }
    }
}
