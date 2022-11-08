package com.ruchij.dao.job;

import com.ruchij.dao.job.models.Job;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface JobDao {
    CompletableFuture<String> insert(Job job);

    CompletableFuture<Optional<Job>> findById(String jobId);
}
