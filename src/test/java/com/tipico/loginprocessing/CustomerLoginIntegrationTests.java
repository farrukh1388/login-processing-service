package com.tipico.loginprocessing;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.tipico.loginprocessing.dto.CustomerLoginEvent;
import com.tipico.loginprocessing.dto.LoginTrackingResultEvent;
import com.tipico.loginprocessing.kafka.TestProducerListener;
import com.tipico.loginprocessing.repository.LoginTrackingResultRepository;
import com.tipico.loginprocessing.testcontainers.PostgresTestContainer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({CustomerLoginTestConfiguration.class})
@ActiveProfiles("test")
@Testcontainers
@EnableWireMock
class CustomerLoginIntegrationTests extends PostgresTestContainer {
  @Container
  static final KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("apache/kafka:latest"));

  @DynamicPropertySource
  static void overrideProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired private KafkaTemplate<String, Object> kafkaTemplate;
  @Autowired private LoginTrackingResultRepository repository;
  @Autowired private TestProducerListener producerListener;

  @ParameterizedTest(
      name =
          "should receive login event and save it with the {1} result of request to Customer Tracking Service")
  @CsvSource({"200, successful", "500, unsuccessful"})
  void shouldReceiveLoginEventAndSaveItWithTheResultsOfRequestToTrackingService(
      int status, String requestResult) {
    var customerId = UUID.randomUUID();
    var url = "/api/v1/track-logging/" + customerId;
    stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(status)));
    var event =
        CustomerLoginEvent.builder()
            .customerId(customerId)
            .username("test user")
            .client("test client")
            .timestamp(Instant.now())
            .messageId(UUID.randomUUID())
            .customerIp("8.8.8.8")
            .build();
    kafkaTemplate.send("customer-login", event.customerId().toString(), event);

    // assert that login event data with request result is persisted to db
    await()
        .pollInterval(Duration.ofMillis(200))
        .atMost(5, SECONDS)
        .untilAsserted(
            () -> {
              var optionalResult = repository.findByCustomerId(event.customerId());
              assertThat(optionalResult).isPresent();
              var loginTrackingResult = optionalResult.get();
              assertThat(loginTrackingResult)
                  .usingRecursiveComparison()
                  .ignoringFields("id", "requestResult", "timestamp")
                  .isEqualTo(event);
              assertThat(loginTrackingResult.requestResult()).isEqualTo(requestResult);
              assertThat(loginTrackingResult.timestamp().getEpochSecond())
                  .isEqualTo(event.timestamp().getEpochSecond());
            });

    // assert that event with request result is sent to login-tracking-result topic
    await()
        .pollInterval(Duration.ofMillis(200))
        .atMost(5, SECONDS)
        .until(
            () ->
                this.producerListener.getEventsSent().get("login-tracking-result") != null
                    && this.producerListener
                        .getEventsSent()
                        .get("login-tracking-result")
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().equals(event.customerId().toString()))
                        .map(Map.Entry::getValue)
                        .flatMap(List::stream)
                        .anyMatch(
                            eventSent ->
                                eventSent instanceof LoginTrackingResultEvent
                                    && event
                                        .messageId()
                                        .equals(((LoginTrackingResultEvent) eventSent).messageId())
                                    && requestResult.equals(
                                        ((LoginTrackingResultEvent) eventSent).requestResult())));
  }
}

@TestConfiguration
class CustomerLoginTestConfiguration {
  @Bean
  public TestProducerListener customProducerListener() {
    return new TestProducerListener();
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate(
      ProducerFactory<String, Object> producerFactory, TestProducerListener testProducerListener) {
    KafkaTemplate<String, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory);
    kafkaTemplate.setProducerListener(testProducerListener);
    return kafkaTemplate;
  }
}
