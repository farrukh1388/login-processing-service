package com.tipico.loginprocessing.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tipico.loginprocessing.entity.LoginTrackingResult;
import com.tipico.loginprocessing.testcontainers.PostgresTestContainer;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoginTrackingResultRepositoryTests extends PostgresTestContainer {
  @Autowired private LoginTrackingResultRepository repository;

  @Test
  void shouldSuccessfullyCreateLoginTrackingResult() {
    var loginTrackingResult =
        repository.save(
            new LoginTrackingResult()
                .customerId(UUID.randomUUID())
                .username("test user")
                .client("test client")
                .timestamp(Instant.now())
                .messageId(UUID.randomUUID())
                .customerIp("8.8.8.8")
                .requestResult("successful"));

    assertThat(repository.getReferenceById(loginTrackingResult.id()))
        .usingRecursiveComparison()
        .isEqualTo(loginTrackingResult);
  }
}
