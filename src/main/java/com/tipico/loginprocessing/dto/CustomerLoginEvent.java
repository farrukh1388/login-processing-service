package com.tipico.loginprocessing.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CustomerLoginEvent(
    UUID customerId,
    String username,
    String client,
    Instant timestamp,
    UUID messageId,
    String customerIp) {}
