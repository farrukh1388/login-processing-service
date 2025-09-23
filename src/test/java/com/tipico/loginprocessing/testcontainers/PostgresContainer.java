package com.tipico.loginprocessing.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {
  private static final String IMAGE_VERSION = "postgres:17.0";
  private static PostgresContainer container;

  private PostgresContainer() {
    super(IMAGE_VERSION);
  }

  public static PostgresContainer getInstance() {
    if (container == null) {
      container = new PostgresContainer();
    }
    return container;
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  public void stop() {
    // do nothing, JVM handles shut down
  }
}
