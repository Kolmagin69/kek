package ru.innotech.integration.testing.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class PendingRequestsRegistry {

    private final ConcurrentHashMap<String, CompletableFuture<Map<String, Object>>> pending = new ConcurrentHashMap<>();

    public CompletableFuture<Map<String, Object>> register(String testId) {
        var future = new CompletableFuture<Map<String, Object>>();
        pending.put(testId, future);
        return future;
    }

    public void complete(String testId, Map<String, Object> result) {
        var future = pending.remove(testId);
        if (future != null) future.complete(result);
    }

    public void completeExceptionally(String testId, Throwable ex) {
        var future = pending.remove(testId);
        if (future != null) future.completeExceptionally(ex);
    }

    public Map<String, Object> await(String testId, long timeout, TimeUnit unit) throws Exception {
        var future = pending.get(testId);
        if (future == null) throw new IllegalStateException("Нет ожидающего запроса testId=" + testId);
        try {
            return future.get(timeout, unit);
        } finally {
            pending.remove(testId);
        }
    }
}
