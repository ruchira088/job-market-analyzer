package com.ruchij.api.services.health.models;

import java.time.Instant;

public record BuildInformation(String name, String group, String version, String gradleVersion, Instant buildTimestamp,
                               String gitBranch, String gitCommit) {
}
