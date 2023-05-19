package com.ruchij.api.web.responses;

import java.util.Collection;

public record PaginatedResponse<T>(int pageSize, int pageNumber, Collection<T> results) {
}
