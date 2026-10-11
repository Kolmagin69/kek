package ru.innotech.integration.testing.exception;
public class ChunkSendException extends RuntimeException {
    public ChunkSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
