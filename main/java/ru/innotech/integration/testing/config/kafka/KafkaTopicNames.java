package ru.innotech.integration.testing.config.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.innotech.integration.testing.config.props.PimKafkaProperties;

@Configuration
public class KafkaTopicNames {

  private final PimKafkaProperties props;

  public KafkaTopicNames(PimKafkaProperties props) {
    this.props = props;
  }

  @Bean("pimGetTopicName")
  public String pimGetTopicName() {
    return props.getGetTopic();
  }
}
