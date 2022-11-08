package com.ruchij.dao.task;

import com.ruchij.dao.task.models.CrawlerTask;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.random.RandomGenerator;
import com.ruchij.test.ElasticsearchTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static com.ruchij.test.TestUtils.waitFor;

class ElasticsearchCrawlerTaskDaoTest {

    @Test
    void performOperations() throws Exception {
        ElasticsearchTest elasticsearchTest = elasticsearchAsyncClient -> {
            ElasticsearchCrawlerTaskDao elasticsearchCrawlerTaskDao =
                new ElasticsearchCrawlerTaskDao(elasticsearchAsyncClient);

            String crawlerTaskId = RandomGenerator.idGenerator().generate();

            Clock clock = Clock.systemClock();
            Instant startTimestamp = clock.timestamp();

            CrawlerTask crawlerTask = new CrawlerTask();
            crawlerTask.setCrawlerId(crawlerTaskId);
            crawlerTask.setStartedAt(startTimestamp);

            String insertionResult = waitFor(elasticsearchCrawlerTaskDao.insert(crawlerTask));
            Assertions.assertEquals(crawlerTaskId, insertionResult);

            Optional<CrawlerTask> maybeCrawlerTask = waitFor(elasticsearchCrawlerTaskDao.findById(crawlerTaskId));

            Assertions.assertTrue(maybeCrawlerTask.isPresent());
            Assertions.assertEquals(crawlerTask, maybeCrawlerTask.get());

            Instant finishTimestamp = clock.timestamp();
            crawlerTask.setFinishedAt(finishTimestamp);

            Boolean updated =
                waitFor(elasticsearchCrawlerTaskDao.setFinishedTimestamp(crawlerTaskId, finishTimestamp));

            Assertions.assertTrue(updated);

            Optional<CrawlerTask> maybeFinishedCrawlerTask =
                waitFor(elasticsearchCrawlerTaskDao.findById(crawlerTaskId));

            Assertions.assertTrue(maybeFinishedCrawlerTask.isPresent());
            Assertions.assertEquals(crawlerTask, maybeFinishedCrawlerTask.get());

            String randomId = RandomGenerator.idGenerator().generate();

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