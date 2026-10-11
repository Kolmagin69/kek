package ru.innotech.integration.testing.config.props;

import lombok.Data;

@Data
public class BaseKafkaProperties {

  private String bootstrapServers;

  private Producer producer = new Producer();

  private Consumer consumer = new Consumer();

  @Data
  public static class Producer {
    private String transactionIdPrefix;
  }

  @Data
  public static class Consumer {
    private String groupId;
    private String autoOffsetReset;
  }
}