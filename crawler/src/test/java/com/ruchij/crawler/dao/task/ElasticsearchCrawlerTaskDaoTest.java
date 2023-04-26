package com.ruchij.crawler.dao.task;

import com.ruchij.crawler.dao.task.models.CrawlerTask;
import com.ruchij.crawler.dao.transaction.ElasticsearchTransactor;
import com.ruchij.crawler.service.random.RandomGenerator;
import com.ruchij.test.ElasticsearchTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.ruchij.test.TestUtils.waitFor;

class ElasticsearchCrawlerTaskDaoTest {

	@Test
	void performOperations() throws Exception {
		ElasticsearchTest elasticsearchTest = elasticsearchAsyncClient -> {
			ElasticsearchCrawlerTaskDao elasticsearchCrawlerTaskDao =
				new ElasticsearchCrawlerTaskDao(elasticsearchAsyncClient, "local");

			ElasticsearchTransactor elasticsearchTransactor = new ElasticsearchTransactor();

			RandomGenerator<String> idGenerator = RandomGenerator.uuidGenerator().map(UUID::toString);
			String crawlerTaskId = idGenerator.generate();
			String userId = idGenerator.generate();

			Instant startTimestamp = Instant.parse("2023-01-22T07:01:14.050Z");
			CrawlerTask crawlerTask = new CrawlerTask(crawlerTaskId, userId, startTimestamp, Optional.empty());

			String insertionResult = waitFor(elasticsearchTransactor.transaction(elasticsearchCrawlerTaskDao.insert(crawlerTask)));
			Assertions.assertEquals(crawlerTaskId, insertionResult);

			Optional<CrawlerTask> maybeCrawlerTask = waitFor(elasticsearchTransactor.transaction(elasticsearchCrawlerTaskDao.findById(crawlerTaskId)));

			Assertions.assertTrue(maybeCrawlerTask.isPresent());
			Assertions.assertEquals(crawlerTask, maybeCrawlerTask.get());

			Instant finishTimestamp = Instant.parse("2023-01-22T08:01:14.050Z");
			CrawlerTask finishedCrawlerTask =
				new CrawlerTask(crawlerTaskId, userId, startTimestamp, Optional.of(finishTimestamp));

			Boolean updated =
				waitFor(
					elasticsearchTransactor.transaction(elasticsearchCrawlerTaskDao.setFinishedTimestamp(crawlerTaskId, finishTimestamp)));

			Assertions.assertTrue(updated);

			Optional<CrawlerTask> maybeFinishedCrawlerTask =
				waitFor(elasticsearchTransactor.transaction(elasticsearchCrawlerTaskDao.findById(crawlerTaskId)));

			Assertions.assertTrue(maybeFinishedCrawlerTask.isPresent());
			Assertions.assertEquals(finishedCrawlerTask, maybeFinishedCrawlerTask.get());

			String randomId = RandomGenerator.uuidGenerator().map(UUID::toString).generate();

			Optional<CrawlerTask> emptyTask =
				waitFor(elasticsearchTransactor.transaction(elasticsearchCrawlerTaskDao.findById(randomId)));

			Assertions.assertTrue(emptyTask.isEmpty());

			Boolean updatedNonExisting =
				waitFor(elasticsearchTransactor.transaction(elasticsearchCrawlerTaskDao.setFinishedTimestamp(randomId, finishTimestamp)));

			Assertions.assertFalse(updatedNonExisting);
		};

		ElasticsearchTest.run(elasticsearchTest);
	}

}