package com.example.mongodbrest.config;

import com.github.dzieciou.testing.curl.CurlRestAssuredConfigFactory;
import com.github.dzieciou.testing.curl.Options;
import io.restassured.config.RestAssuredConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestBeans {

  @Bean
  public RestAssuredConfig curlConfig() {
    Options curlLoggerOptions = Options.builder()
        .printMultiliner()
        .useLongForm()
        .updateCurl(
            curl -> curl.removeHeader("Host").removeHeader("User-Agent").removeHeader("Connection"))
        .build();

    return CurlRestAssuredConfigFactory.createConfig(curlLoggerOptions);
  }
}
