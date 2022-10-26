package com.ruchij.dao.task;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import com.ruchij.config.ElasticsearchConfiguration;
import com.ruchij.dao.elasticsearch.ElasticsearchClientBuilder;
import com.ruchij.dao.task.models.CrawlerTask;
import com.ruchij.service.clock.Clock;
import com.ruchij.service.random.RandomGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.time.Instant;
import java.util.Optional;

import static com.ruchij.test.TestUtils.waitFor;

class ElasticsearchCrawlerTaskDaoTest {

    @Test
    void performOperations() throws Exception {
        try (ElasticsearchContainer elasticsearchContainer =
                 new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.4.3")
                     .withEnv("xpack.security.enabled", "false")
        ) {
            elasticsearchContainer.start();

            String host = elasticsearchContainer.getHost();
            Integer port = elasticsearchContainer.getMappedPort(9200);

            ElasticsearchConfiguration elasticsearchConfiguration = new ElasticsearchConfiguration(host, port);

            try (ElasticsearchClientBuilder elasticsearchClientBuilder = new ElasticsearchClientBuilder(elasticsearchConfiguration)) {
                ElasticsearchAsyncClient elasticsearchAsyncClient = elasticsearchClientBuilder.buildAsyncClient();
                ElasticsearchCrawlerTaskDao elasticsearchCrawlerTaskDao = new ElasticsearchCrawlerTaskDao(elasticsearchAsyncClient);

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
            }
        }
    }

}