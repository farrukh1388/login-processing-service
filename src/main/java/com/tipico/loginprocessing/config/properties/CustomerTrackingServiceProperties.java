package com.tipico.loginprocessing.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "customer-tracking-service")
public class CustomerTrackingServiceProperties {
  private Integer connectTimeout;
  private Integer connectionRequestTimeout;
  private Integer readTimeout;
  private String username;
  private String password;
  private String baseUri;
  private String trackLoggingPath;
}
