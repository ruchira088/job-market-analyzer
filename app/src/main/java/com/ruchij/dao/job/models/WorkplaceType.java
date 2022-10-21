package com.ruchij.dao.job.models;

import java.util.Arrays;
import java.util.Optional;

public enum WorkplaceType {
    REMOTE, HYBRID, ON_SITE;

    public static Optional<WorkplaceType> parse(String input) {
        return Arrays.stream(WorkplaceType.values())
            .filter(workplaceType -> workplaceType.name().replace('_', '-').equalsIgnoreCase(input))
            .findFirst();
    }
}
