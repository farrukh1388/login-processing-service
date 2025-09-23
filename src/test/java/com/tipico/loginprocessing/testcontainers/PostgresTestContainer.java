package com.tipico.loginprocessing.testcontainers;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgresTestContainer {
  @Container @ServiceConnection
  public static PostgresContainer postgresContainer = PostgresContainer.getInstance();
}
