package ru.innotech.integration.testing.config.props;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties(prefix = "kafka.pim")
public class PimKafkaProperties extends BaseKafkaProperties {

  private String sendTopic;

  private String getTopic;

  private Integer chunkBytesSize;

}