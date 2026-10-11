package ru.innotech.integration.testing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
@EnableTransactionManagement
@ConfigurationPropertiesScan
@EnableScheduling
@EnableConfigurationProperties
@EnableFeignClients
public class IntegrationTestingApplication {

  public static void main(String[] args) {
    SpringApplication.run(IntegrationTestingApplication.class, args);
  }
}
