package ru.innotech.integration.testing.config.kafka;

import org.springframework.beans.factory.annotation.Qualifier;
import ru.innotech.integration.testing.config.props.PimKafkaProperties;
import ru.innotech.integration.testing.exception.ChunkSendException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class KafkaProducerService {

  private final KafkaTemplate<String, byte[]> kafkaTemplate;

  private final PimKafkaProperties pimKafkaProperties;

  public KafkaProducerService(
      final @Qualifier("pimKafkaTemplate") KafkaTemplate<String, byte[]> kafkaTemplate,
      final PimKafkaProperties pimKafkaProperties) {
    this.kafkaTemplate = kafkaTemplate;
    this.pimKafkaProperties = pimKafkaProperties;
  }

  public void sendChunk(
      String msgId,
      String testId,
      String projectId,
      String historyProjectId,
      int currentChunk,
      int totalChunks,
      byte[] payload,
      boolean isLast) {
    ProducerRecord<String, byte[]> record =
        new ProducerRecord<>(pimKafkaProperties.getSendTopic(), testId, payload);

    addHeader(record, KafkaHeaders.GUID, testId);
    addHeader(record, KafkaHeaders.IS_CHUNKS, String.valueOf(totalChunks > 1));
    addHeader(record, KafkaHeaders.CURRENT_CHUNK, String.valueOf(currentChunk));
    addHeader(record, KafkaHeaders.TOTAL_CHUNKS, String.valueOf(totalChunks));
    addHeader(record, KafkaHeaders.IS_LAST, String.valueOf(isLast));
    addHeader(record, KafkaHeaders.MSG_ID, msgId);
    addHeader(record, KafkaHeaders.HISTORY_PROJECT_ID, historyProjectId);

    try {
      kafkaTemplate.send(record).get(15, TimeUnit.SECONDS);
    } catch (Exception e) {
      Thread.currentThread().interrupt();
      throw new ChunkSendException("Failed to send chunk " + currentChunk + "/" + totalChunks, e);
    }
  }

  private void addHeader(ProducerRecord<String, byte[]> record, String key, String value) {
    if (value != null) {
      record.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
    }
  }
}
