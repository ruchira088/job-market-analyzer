package com.ruchij.service.clock;

import java.time.Instant;

public class SystemClock implements Clock {
    @Override
    public Instant timestamp() {
        return Instant.now();
    }
}
