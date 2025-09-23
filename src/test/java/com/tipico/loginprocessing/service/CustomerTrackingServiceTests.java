package com.tipico.loginprocessing.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.http.Fault;
import com.tipico.loginprocessing.testcontainers.PostgresTestContainer;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@EnableWireMock
class CustomerTrackingServiceTests extends PostgresTestContainer {
  @Autowired private CustomerTrackingService customerTrackingService;

  @Test
  void shouldReturnTrueWhenRequestIsSuccessful() {
    var customerId = UUID.randomUUID();
    var url = "/api/v1/track-logging/" + customerId;
    stubFor(post(urlEqualTo(url)).withBasicAuth("username", "password").willReturn(ok()));

    boolean result = customerTrackingService.sendTrackLoggingRequest(customerId);

    assertThat(result).isTrue();
  }

  @Test
  void shouldRetry2TimesBeforeGettingSuccessfulResponse() {
    var customerId = UUID.randomUUID();
    var url = "/api/v1/track-logging/" + customerId;
    var scenario = UUID.randomUUID().toString();
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("First retry"));
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs("First retry")
            .willReturn(aResponse().withStatus(501))
            .willSetStateTo("Second retry"));
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs("Second retry")
            .willReturn(aResponse().withStatus(502))
            .willSetStateTo("Third retry"));
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs("Third retry")
            .willReturn(ok()));

    boolean result = customerTrackingService.sendTrackLoggingRequest(customerId);

    assertThat(result).isTrue();
  }

  @Test
  void shouldRetry3TimesBeforeGettingException() {
    var customerId = UUID.randomUUID();
    var url = "/api/v1/track-logging/" + customerId;
    var scenario = UUID.randomUUID().toString();
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(500))
            .willSetStateTo("First retry"));
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs("First retry")
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
            .willSetStateTo("Second retry"));
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs("Second retry")
            .willReturn(aResponse().withStatus(502))
            .willSetStateTo("Third retry"));
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs("Third retry")
            .willReturn(aResponse().withStatus(503)));

    var thrown =
        assertThrows(
            HttpServerErrorException.class,
            () -> customerTrackingService.sendTrackLoggingRequest(customerId));

    assertThat(thrown.getMessage()).contains("503 Service Unavailable");
  }

  @Test
  void shouldNotRetryForHttpClientException() {
    var customerId = UUID.randomUUID();
    var url = "/api/v1/track-logging/" + customerId;
    var scenario = UUID.randomUUID().toString();
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse().withStatus(400))
            .willSetStateTo("First retry"));
    stubFor(
        post(urlEqualTo(url))
            .inScenario(scenario)
            .whenScenarioStateIs("First retry")
            .willReturn(aResponse().withStatus(500)));

    var thrown =
        assertThrows(
            HttpClientErrorException.class,
            () -> customerTrackingService.sendTrackLoggingRequest(customerId));

    assertThat(thrown.getMessage()).contains("400 Bad Request");
  }
}
