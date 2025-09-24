package com.tipico.loginprocessing.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record LoginTrackingResultEvent(
    UUID customerId,
    String username,
    String client,
    Instant timestamp,
    UUID messageId,
    String customerIp,
    String requestResult) {}
