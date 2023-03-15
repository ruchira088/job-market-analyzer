package com.ruchij.api.services.search;

import com.ruchij.api.dao.job.SearchableJobDao;
import com.ruchij.crawler.dao.job.models.Job;
import com.ruchij.crawler.dao.task.CrawlerTaskDao;
import com.ruchij.crawler.dao.task.models.CrawlerTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchServiceImpl implements SearchService {
	private final SearchableJobDao searchableJobDao;
	private final CrawlerTaskDao crawlerTaskDao;

	public SearchServiceImpl(SearchableJobDao searchableJobDao, CrawlerTaskDao crawlerTaskDao) {
		this.searchableJobDao = searchableJobDao;
		this.crawlerTaskDao = crawlerTaskDao;
	}

	@Override
	public CompletableFuture<List<Job>> findJobsByCrawlerTaskId(String crawlerTaskId, int pageSize, int pageNumber) {
		return this.searchableJobDao.findByCrawlerTaskId(crawlerTaskId, pageSize, pageNumber);
	}

	@Override
	public CompletableFuture<Long> countJobsByCrawlerTaskId(String crawlerTaskId) {
		return this.searchableJobDao.countJobsByCrawlerTaskId(crawlerTaskId);
	}

	@Override
	public CompletableFuture<List<CrawlerTask>> findCrawlerTasksByUserId(String userId, int pageSize, int pageNumber) {
		return this.crawlerTaskDao.findByUserId(userId, pageSize, pageNumber);
	}
}
