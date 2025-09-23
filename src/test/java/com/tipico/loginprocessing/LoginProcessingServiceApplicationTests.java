package com.tipico.loginprocessing;

import com.tipico.loginprocessing.testcontainers.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LoginProcessingServiceApplicationTests extends PostgresTestContainer {

  @Test
  void contextLoads() {}
}
