package ru.innotech.integration.testing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.innotech.integration.testing.config.props.PimKafkaProperties;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChunkSplitter {

  private final PimKafkaProperties pimKafkaProperties;

  public List<byte[]> split(byte[] data) {
    return split(data, pimKafkaProperties.getChunkBytesSize());
  }

  public List<byte[]> split(byte[] data, int maxBytes) {
    if (data == null || data.length == 0) {
      return List.of(new byte[0]);
    }
    List<byte[]> chunks = new ArrayList<>();
    int start = 0;
    while (start < data.length) {
      int end = Math.min(start + maxBytes, data.length);
      while (end > start && end < data.length && (isUtf8ContinuationByte(data[end]))) {
        end--;
      }
      byte[] chunk = new byte[end - start];
      System.arraycopy(data, start, chunk, 0, end - start);
      chunks.add(chunk);
      start = end;
    }
    return chunks;
  }

  /**
   * Проверяет, является ли байт байтом-продолжением в кодировке UTF-8. В UTF-8 вс байты-продолжения
   * имеют вид 10xxxxxx. Маска 0xC0 (11000000) выделяет первые два бита. Сравнение с 0x80 (10000000)
   * подтверждает, что это '10'.
   */
  private boolean isUtf8ContinuationByte(byte value) {
    return (value & 0xC0) == 0x80;
  }
}
