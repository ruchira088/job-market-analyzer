package com.ruchij.service.clock;

import java.time.Instant;

public interface Clock {
    Instant timestamp();

    static Clock systemClock() {
        return Instant::now;
    }
}
