package com.ruchij.crawler.dao.task;

import com.ruchij.crawler.dao.task.models.CrawlerTask;
import com.ruchij.crawler.service.clock.Clock;
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
                new ElasticsearchCrawlerTaskDao(elasticsearchAsyncClient);

            RandomGenerator<String> idGenerator = RandomGenerator.uuidGenerator().map(UUID::toString);
            String crawlerTaskId = idGenerator.generate();
            String userId = idGenerator.generate();

            Clock clock = Clock.systemClock();
            Instant startTimestamp = clock.timestamp();

            CrawlerTask crawlerTask = new CrawlerTask(crawlerTaskId, userId, startTimestamp, Optional.empty());

            String insertionResult = waitFor(elasticsearchCrawlerTaskDao.insert(crawlerTask));
            Assertions.assertEquals(crawlerTaskId, insertionResult);

            Optional<CrawlerTask> maybeCrawlerTask = waitFor(elasticsearchCrawlerTaskDao.findById(crawlerTaskId));

            Assertions.assertTrue(maybeCrawlerTask.isPresent());
            Assertions.assertEquals(crawlerTask, maybeCrawlerTask.get());

            Instant finishTimestamp = clock.timestamp();
            CrawlerTask finishedCrawlerTask =
                new CrawlerTask(crawlerTaskId, userId, startTimestamp, Optional.of(finishTimestamp));

            Boolean updated =
                waitFor(elasticsearchCrawlerTaskDao.setFinishedTimestamp(crawlerTaskId, finishTimestamp));

            Assertions.assertTrue(updated);

            Optional<CrawlerTask> maybeFinishedCrawlerTask =
                waitFor(elasticsearchCrawlerTaskDao.findById(crawlerTaskId));

            Assertions.assertTrue(maybeFinishedCrawlerTask.isPresent());
            Assertions.assertEquals(finishedCrawlerTask, maybeFinishedCrawlerTask.get());

            String randomId = RandomGenerator.uuidGenerator().map(UUID::toString).generate();

            Optional<CrawlerTask> emptyTask =
                waitFor(elasticsearchCrawlerTaskDao.findById(randomId));

            Assertions.assertTrue(emptyTask.isEmpty());

            Boolean updatedNonExisting =
                waitFor(elasticsearchCrawlerTaskDao.setFinishedTimestamp(randomId, finishTimestamp));

            Assertions.assertFalse(updatedNonExisting);
        };

        ElasticsearchTest.run(elasticsearchTest);
    }

}