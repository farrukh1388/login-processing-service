package com.tipico.loginprocessing.config;

import com.tipico.loginprocessing.config.properties.CustomerTrackingServiceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CustomerTrackingServiceProperties.class)
@EnableRetry
public class RestClientConfig {

  @Bean(name = "ctsRestClient")
  public RestClient restClient(CustomerTrackingServiceProperties properties) {
    var requestFactory = new HttpComponentsClientHttpRequestFactory();
    requestFactory.setConnectionRequestTimeout(properties.getConnectionRequestTimeout());
    requestFactory.setConnectTimeout(properties.getConnectTimeout());
    requestFactory.setReadTimeout(properties.getReadTimeout());

    return RestClient.builder()
        .requestFactory(requestFactory)
        .baseUrl(properties.getBaseUri())
        .defaultHeaders(it -> it.setBasicAuth(properties.getUsername(), properties.getPassword()))
        .build();
  }
}
