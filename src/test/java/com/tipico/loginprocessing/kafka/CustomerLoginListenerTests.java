package com.tipico.loginprocessing.kafka;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.tipico.loginprocessing.dto.CustomerLoginEvent;
import com.tipico.loginprocessing.repository.LoginTrackingResultRepository;
import com.tipico.loginprocessing.testcontainers.PostgresTestContainer;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class CustomerLoginListenerTests extends PostgresTestContainer {
  @Container
  static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
  @Autowired private LoginTrackingResultRepository repository;

  @Test
  void shouldReturnTrueWhenRequestIsSuccessful() {
    var event =
        CustomerLoginEvent.builder()
            .customerId(UUID.randomUUID())
            .username("test user")
            .client("test client")
            .timestamp(Instant.now())
            .messageId(UUID.randomUUID())
            .customerIp("8.8.8.8")
            .build();
    kafkaTemplate.send("customer-login", event.customerId().toString(), event);

    await()
        .pollInterval(Duration.ofMillis(200))
        .atMost(10, SECONDS)
        .untilAsserted(
            () -> {
              var optionalResult = repository.findByCustomerId(event.customerId());
              assertThat(optionalResult).isPresent();
              var loginTrackingResult = optionalResult.get();
              assertThat(loginTrackingResult)
                  .usingRecursiveComparison()
                  .ignoringFields("id", "requestResult", "timestamp")
                  .isEqualTo(event);
              assertThat(loginTrackingResult.requestResult()).isEqualTo("successful");
              assertThat(loginTrackingResult.timestamp().getEpochSecond())
                  .isEqualTo(event.timestamp().getEpochSecond());
            });
  }
}
