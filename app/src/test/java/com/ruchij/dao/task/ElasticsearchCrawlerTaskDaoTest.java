package com.ruchij.dao.task;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.concurrent.TimeUnit;

class ElasticsearchCrawlerTaskDaoTest {

    @Test
    void insertCrawlerTasks() throws Exception {
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

                String insertionResult = elasticsearchCrawlerTaskDao.insert(crawlerTask).get(10, TimeUnit.SECONDS);
                Assertions.assertEquals(crawlerTaskId, insertionResult);

                Optional<CrawlerTask> maybeCrawlerTask = elasticsearchCrawlerTaskDao.findById(crawlerTaskId).get(10, TimeUnit.SECONDS);
                Assertions.assertTrue(maybeCrawlerTask.isPresent());

                Assertions.assertEquals(crawlerTask, maybeCrawlerTask.get());
            }

        }
    }

}