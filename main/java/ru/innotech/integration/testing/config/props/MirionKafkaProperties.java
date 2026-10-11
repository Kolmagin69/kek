package ru.innotech.integration.testing.config.props;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "kafka.mirion")
public class MirionKafkaProperties extends BaseKafkaProperties {

  private String resultTopic;
}
