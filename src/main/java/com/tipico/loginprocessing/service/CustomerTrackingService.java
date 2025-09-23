package com.tipico.loginprocessing.service;

import com.tipico.loginprocessing.config.properties.CustomerTrackingServiceProperties;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerTrackingService {
  private final RestClient ctsRestClient;
  private final CustomerTrackingServiceProperties properties;

  @Retryable(
      retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
      maxAttemptsExpression = "${customer-tracking-service.retry.maxAttempts}",
      backoff =
          @Backoff(
              delayExpression = "${customer-tracking-service.retry.delay}",
              multiplierExpression = "${customer-tracking-service.retry.multiplier}"))
  public boolean sendTrackLoggingRequest(UUID customerId) {
    try {
      var bodilessEntity =
          ctsRestClient
              .post()
              .uri(properties.getTrackLoggingPath().replace("{customerId}", customerId.toString()))
              .contentType(MediaType.APPLICATION_JSON)
              .retrieve()
              .toBodilessEntity();
      return bodilessEntity.getStatusCode() == HttpStatus.OK;
    } catch (HttpStatusCodeException ex) {
      log.error(
          "Track logging request failed with status {}: {}", ex.getStatusCode(), ex.getMessage());
      throw ex;
    } catch (RestClientException ex) {
      log.error("Track logging request failed: {}", ex.getMessage());
      throw ex;
    }
  }
}
