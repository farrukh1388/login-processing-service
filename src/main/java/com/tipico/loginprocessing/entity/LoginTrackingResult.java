package com.tipico.loginprocessing.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "login_tracking_results")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Accessors(fluent = true, chain = true)
public class LoginTrackingResult {
  @Id
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  @EqualsAndHashCode.Include
  private UUID id;

  @Column(name = "customer_id")
  @EqualsAndHashCode.Include
  private UUID customerId;

  @Column(name = "username")
  private String username;

  @Column(name = "client")
  private String client;

  @Column(name = "timestamp")
  private Instant timestamp;

  @Column(name = "message_id")
  @EqualsAndHashCode.Include
  private UUID messageId;

  @Column(name = "customer_ip")
  private String customerIp;

  @Column(name = "request_result")
  private String requestResult;
}
