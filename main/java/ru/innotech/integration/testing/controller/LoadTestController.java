package ru.innotech.integration.testing.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.innotech.integration.testing.dto.LoadTestRequest;
import ru.innotech.integration.testing.service.LoadTestSendService;
import ru.innotech.integration.testing.service.PendingRequestsRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/load-test")
@RequiredArgsConstructor
@Slf4j
public class LoadTestController {

  private final LoadTestSendService sendService;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Long start(@Valid @RequestBody LoadTestRequest request) throws Exception {
    var testId = UUID.randomUUID().toString();
    var msgId = UUID.randomUUID().toString();

    sendService.send(testId, msgId, request);
    return 0L;
  }
}
