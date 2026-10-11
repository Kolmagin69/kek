package ru.innotech.integration.testing.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "kafka.common.ssl")
public class KafkaSslProperties {

  private boolean enabled;

  private String protocol;

  private String trustStoreLocation;

  private String trustStorePassword;

  private String keyStoreLocation;

  private String keyStorePassword;

  private String keyStoreType;

  private String trustStoreType;
}
