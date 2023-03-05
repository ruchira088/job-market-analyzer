package com.ruchij.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TestUtils {
	public static <T> T waitFor(Future<T> future) throws ExecutionException, InterruptedException, TimeoutException {
		return future.get(10, TimeUnit.SECONDS);
	}
}
