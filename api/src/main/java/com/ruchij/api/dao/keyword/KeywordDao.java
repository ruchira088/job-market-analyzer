package com.ruchij.api.dao.keyword;

import com.ruchij.api.dao.keyword.models.Keyword;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface KeywordDao {
	CompletableFuture<String> insert(Keyword keyword);

	CompletableFuture<List<Keyword>> getByUserId(String userId, int pageSize, int pageNumber);
}
