package com.tipico.loginprocessing.service;

import com.tipico.loginprocessing.dto.CustomerLoginEvent;
import com.tipico.loginprocessing.dto.LoginTrackingResultEvent;
import com.tipico.loginprocessing.entity.LoginTrackingResult;
import com.tipico.loginprocessing.repository.LoginTrackingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginProcessingService {
  private final CustomerTrackingService customerTrackingService;
  private final KafkaTemplate<String, Object> kafkaTemplate;
  private final LoginTrackingResultRepository repository;

  public void process(CustomerLoginEvent loginEvent) {
    // send track-logging request to Customer Tracking Service
    var requestOk = false;
    try {
      requestOk = customerTrackingService.sendTrackLoggingRequest(loginEvent.customerId());
    } catch (RestClientException e) {
      log.warn(
          "All retries to send track logging request for customer {} failed: {}",
          loginEvent.customerId(),
          e.getMessage());
    }
    var requestResult = requestOk ? "successful" : "unsuccessful";

    var resultEvent =
        LoginTrackingResultEvent.builder()
            .customerId(loginEvent.customerId())
            .username(loginEvent.username())
            .client(loginEvent.client())
            .timestamp(loginEvent.timestamp())
            .messageId(loginEvent.messageId())
            .customerIp(loginEvent.customerIp())
            .requestResult(requestResult)
            .build();
    // publish to Kafka (best-effort)
    try {
      kafkaTemplate.send("login-tracking-result", loginEvent.customerId().toString(), resultEvent);
    } catch (Exception e) {
      log.error("Failed to publish result to Kafka: {}", e.getMessage());
    }

    // persist
    repository.save(
        new LoginTrackingResult()
            .customerId(loginEvent.customerId())
            .username(loginEvent.username())
            .client(loginEvent.client())
            .timestamp(loginEvent.timestamp())
            .messageId(loginEvent.messageId())
            .customerIp(loginEvent.customerIp())
            .requestResult(requestResult));

    log.info(
        "Processed login for customer {} with result={}", loginEvent.customerId(), requestResult);
  }
}
