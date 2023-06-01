package com.ruchij.api.services.lock;

import com.ruchij.crawler.containers.JdbiContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DatabaseLockServiceTest {
	@Container
	private final JdbiContainer jdbiContainer = new JdbiContainer();



}