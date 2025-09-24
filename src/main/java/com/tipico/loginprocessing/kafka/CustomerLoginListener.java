package com.tipico.loginprocessing.kafka;

import com.tipico.loginprocessing.dto.CustomerLoginEvent;
import com.tipico.loginprocessing.service.LoginProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerLoginListener {
  private final LoginProcessingService loginProcessingService;

  @KafkaListener(topics = "customer-login", groupId = "login-processing-group")
  public void listen(CustomerLoginEvent event) {
    try {
      loginProcessingService.process(event);
    } catch (Exception e) {
      log.error("Failed to process incoming kafka message", e);
    }
  }
}
