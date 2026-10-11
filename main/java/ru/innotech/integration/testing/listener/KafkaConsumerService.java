package ru.innotech.integration.testing.listener;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.innotech.integration.testing.config.kafka.KafkaHeaders;
import ru.innotech.integration.testing.config.props.PimKafkaProperties;
import ru.innotech.integration.testing.service.LoadTestReceiveService;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

  private final LoadTestReceiveService receiveService;

  @KafkaListener(topics = "#{@pimGetTopicName}", containerFactory = "pimContainerFactory")
  public void onMessage(ConsumerRecord<String, byte[]> record) {
    String testId = header(record, KafkaHeaders.GUID);
    String historyProjectId = header(record, KafkaHeaders.HISTORY_PROJECT_ID);
    int chunkIndex = parseInt(header(record, KafkaHeaders.CURRENT_CHUNK));
    int totalChunks = parseInt(header(record, KafkaHeaders.TOTAL_CHUNKS));
    boolean isLast = Boolean.parseBoolean(header(record, KafkaHeaders.IS_LAST));
    byte[] payload = record.value();

    log.debug("Received chunk {}/{} for testId={}", chunkIndex, totalChunks, testId);

    receiveService.handleChunk(historyProjectId, testId, chunkIndex, totalChunks, isLast, payload);
  }

  private String header(ConsumerRecord<String, byte[]> r, String key) {
    var h = r.headers().lastHeader(key);
    return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
  }

  private int parseInt(String v) {
    return v == null ? 0 : Integer.parseInt(v);
  }
}
