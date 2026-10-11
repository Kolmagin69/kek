package ru.innotech.integration.testing.enums;

public enum ChunkStatus {
  PENDING,
  SENT,
  RETRY,
  FAILED,
  RECEIVED,
  ASSEMBLED;

  public boolean isTerminal() {
    return this == SENT || this == FAILED || this == ASSEMBLED;
  }
}
