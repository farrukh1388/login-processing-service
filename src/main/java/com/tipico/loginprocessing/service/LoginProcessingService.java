package com.tipico.loginprocessing.service;

import com.tipico.loginprocessing.dto.CustomerLoginEvent;
import com.tipico.loginprocessing.entity.LoginTrackingResult;
import com.tipico.loginprocessing.repository.LoginTrackingResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginProcessingService {
  private final LoginTrackingResultRepository repository;

  public void process(CustomerLoginEvent event) {
    var result =
        new LoginTrackingResult()
            .customerId(event.customerId())
            .username(event.username())
            .client(event.client())
            .timestamp(event.timestamp())
            .messageId(event.messageId())
            .customerIp(event.customerIp())
            .requestResult("successful");
    repository.save(result);
  }
}
