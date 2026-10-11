package ru.innotech.integration.testing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innotech.client.asyncgateway.model.SubtaskDTO;
import com.innotech.spring.boot.starter.mirion.web.utils.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.innotech.spring.boot.starter.mirion.async.service.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.integration.testing.client.IntegrationTestingServiceClient;
import ru.innotech.integration.testing.config.kafka.KafkaProducerService;
import ru.innotech.integration.testing.dto.LoadTestRequest;
import ru.innotech.integration.testing.entity.LoadTestHistory;
import ru.innotech.integration.testing.entity.LoadTestingDocument;
import ru.innotech.integration.testing.repository.LoadTestHistoryRepository;
import ru.innotech.integration.testing.repository.LoadTestingRepository;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadTestSendService {

  private final LoadTestHistoryRepository historyRepo;

  private final LoadTestingRepository arangoRepo;

  private final KafkaProducerService kafkaProducer;

  private final IntegrationTestingServiceClient integrationTestingServiceClient;

  private final AsyncTaskExecutor asyncTaskExecutor;

  private final ChunkSplitter chunkSplitter;

  private final ObjectMapper mapper;

  private static final int PREVIEW_LENGTH = 500;

  @Transactional
  public void send(String testId, String msgId, LoadTestRequest request) {

    arangoRepo.save(toDocument(testId, request));
    var projectId = request.getProjectId();

    var history = new LoadTestHistory();
    history.setProjectid(projectId);
    history.setTestid(testId);
    history.startSending();
    historyRepo.save(history);

    var fullBytes = getBytes(testId, request, history);

    var chunks = chunkSplitter.split(fullBytes);
    log.info(
        "Тест {} {} → {} чанков ({} байт)",
        request.getTestType(),
        testId,
        chunks.size(),
        fullBytes.length);

    for (var i = 0; i < chunks.size(); i++) {
      try {
        kafkaProducer.sendChunk(
            msgId,
            testId,
            projectId,
            request.getHistoryProjectId(),
            i,
            chunks.size(),
            chunks.get(i),
            i == chunks.size() - 1);
      } catch (Exception e) {
        log.error("Чанк {}/{} теста {} не отправлен", i, chunks.size(), testId, e);
        history.markSentFailed("Chunk " + i + " failed: " + e.getMessage());
        historyRepo.save(history);
        throw new RuntimeException("Не удалось отправить чанк " + i, e);
      }
    }

    history.markSentComplete(fullBytes.length, preview(fullBytes), Instant.now());
    historyRepo.save(history);
    log.info("Тест {} отправлен: {} чанков", testId, chunks.size());

    var process = integrationTestingServiceClient.createProcess(projectId);
    asyncTaskExecutor.executeTask(
        Objects.requireNonNull(process.getBody()).getSubtaskId(),
        process.getBody().getProcessId(),
        () -> "lol");
  }

  private byte[] getBytes(
      final String testId, final LoadTestRequest request, final LoadTestHistory history) {
    try {
      return mapper.writeValueAsBytes(request.getSendTestAttributes());
    } catch (Exception e) {
      log.error("Ошибка сериализации testId={}", testId, e);
      history.markSentFailed("Serialization error: " + e.getMessage());
      historyRepo.save(history);
      throw new RuntimeException("Не удалось сериализовать payload", e);
    }
  }

  private LoadTestingDocument toDocument(String testId, LoadTestRequest request) {

    var doc = new LoadTestingDocument();
    doc.setId(testId);
    doc.setVertexId(request.getVertexId());
    doc.setProjectId(request.getProjectId());
    doc.setHistoryProjectId(request.getHistoryProjectId());
    doc.setCreatedDate(request.getCreatedDate());
    doc.setUpdatedDate(request.getUpdatedDate());
    doc.setActualDate(request.getActualDate());
    doc.setDescription(request.getDescription());
    doc.setIsProject(request.getIsProject());
    doc.setUserUuid(UserContextUtils.getUserUuid());
    doc.setUserName(UserContextUtils.getUserLogin());
    doc.setTestType(request.getTestType());
    doc.setSendTestAttributes(request.getSendTestAttributes());

    return doc;
  }

  private String preview(byte[] data) {
    return new String(data, 0, Math.min(data.length, PREVIEW_LENGTH), StandardCharsets.UTF_8);
  }
}
