package com.ruchij.test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class TestClock extends Clock {
	private ZoneId zoneId;
	private Instant instant;

	@Override
	public ZoneId getZone() {
		return zoneId;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		TestClock testClock = new TestClock();
		testClock.zoneId = zone;

		return testClock;
	}

	@Override
	public Instant instant() {
		return instant;
	}

	public void setInstant(Instant instant) {
		this.instant = instant;
	}
}
