package ru.innotech.integration.testing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.integration.testing.entity.GetLoadTestMsg;
import ru.innotech.integration.testing.enums.ReceivedStatus;
import ru.innotech.integration.testing.repository.GetLoadTestMsgRepository;
import ru.innotech.integration.testing.repository.LoadTestHistoryRepository;
import ru.innotech.integration.testing.repository.LoadTestingRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadTestReceiveService {

  private static final int PREVIEW_LENGTH = 500;

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

  private final LoadTestHistoryRepository historyRepo;
  private final GetLoadTestMsgRepository getRepo;
  private final LoadTestingRepository arangoRepo;
  private final PendingRequestsRegistry registry;
  private final ObjectMapper mapper;

  @Transactional
  public void handleChunk(
      String projectId,
      String testId,
      int chunkIndex,
      int totalChunks,
      boolean isLast,
      byte[] payload) {

    var history = historyRepo.findByTestid(testId).orElse(null);
    if (history == null) {
      log.warn("История не найдена testId={}", testId);
      return;
    }

    if (history.getReceivedStatus() == ReceivedStatus.PENDING) {
      history.markReceiving();
      historyRepo.save(history);
    }

    var first = getRepo.findFirstByTestidOrderByChunkIndexAsc(testId).orElse(null);
    if (first != null && !first.getTotalChunks().equals(totalChunks)) {
      log.error(
          "Несовпадение totalChunks testId={}: ожидалось {}, пришло {}",
          testId,
          first.getTotalChunks(),
          totalChunks);
      history.markReceiveFailed("totalChunks mismatch");
      historyRepo.save(history);
      return;
    }

    if (saveChunkIfAbsent(projectId, testId, chunkIndex, totalChunks, payload)) {
      historyRepo.incrementReceivedChunks(testId);
    } else {
      log.debug("Дубликат чанка {} testId={}", chunkIndex, testId);
    }

    if (!isLast) return;
    if (!isComplete(testId, totalChunks)) {
      log.debug("Не все чанки получены testId={}", testId);
      return;
    }

    assemble(projectId, testId);
  }

  private boolean saveChunkIfAbsent(
      String projectId, String testId, int chunkIndex, int totalChunks, byte[] payload) {
    var existing = getRepo.findByTestidAndChunkIndex(testId, chunkIndex);
    if (existing.isPresent()) {
      var c = existing.get();
      c.setPayload(payload);
      getRepo.save(c);
      return false;
    }
    var chunk = new GetLoadTestMsg();
    chunk.setProjectid(projectId);
    chunk.setTestid(testId);
    chunk.setChunkIndex(chunkIndex);
    chunk.setTotalChunks(totalChunks);
    chunk.setPayload(payload);
    getRepo.save(chunk);
    return true;
  }

  private boolean isComplete(String testId, int totalChunks) {
    var received = new HashSet<>(getRepo.findChunkIndexesByTestid(testId));
    if (received.size() != totalChunks) return false;
    for (var i = 0; i < totalChunks; i++) if (!received.contains(i)) return false;
    return true;
  }

  private void assemble(String projectId, String testId) {
    var history = historyRepo.findByTestid(testId).orElseThrow();
    try {
      var chunks = getRepo.findByTestidOrderByChunkIndexAsc(testId);
      var fullBytes = concatPayload(chunks);

      Map<String, Object> result = mapper.readValue(fullBytes, MAP_TYPE);

      var now = Instant.now();
      chunks.forEach(c -> c.markAssembled(now));
      getRepo.saveAll(chunks);

      arangoRepo
          .findById(testId)
          .ifPresent(
              doc -> {
                doc.mergeGetResult(result);
                arangoRepo.save(doc);
              });

      history.markReceiveComplete(preview(fullBytes), now);
      historyRepo.save(history);

      registry.complete(testId, result);
      log.info("Тест {} собран: {} чанков", testId, chunks.size());

    } catch (Exception e) {
      log.error("Ошибка сборки testId={}", testId, e);
      history.markReceiveFailed(e.getMessage());
      historyRepo.save(history);
      registry.completeExceptionally(testId, e);
    }
  }

  private byte[] concatPayload(List<GetLoadTestMsg> chunks) throws IOException {
    var baos = new ByteArrayOutputStream();
    for (var c : chunks) if (c.getPayload() != null) baos.write(c.getPayload());
    return baos.toByteArray();
  }

  private String preview(byte[] data) {
    return new String(data, 0, Math.min(data.length, PREVIEW_LENGTH), StandardCharsets.UTF_8);
  }

  @Scheduled(fixedDelay = 60_000)
  @Transactional
  public void failStaleAssemblies() {
    var threshold = Instant.now().minus(10, ChronoUnit.MINUTES);
    var stale =
        historyRepo.findByReceivedStatusAndUpdatedAtBefore(ReceivedStatus.RECEIVING, threshold);
    for (var h : stale) {
      log.warn("Таймаут сборки testId={}", h.getTestid());
      h.markReceiveFailed("Assembly timeout");
      historyRepo.save(h);
      registry.completeExceptionally(h.getTestid(), new TimeoutException("Assembly timeout"));
    }
  }
}
