package ru.innotech.integration.testing.entity;

import ru.innotech.integration.testing.enums.ChunkStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(
    name = "get_load_test_msg",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_get_chunk",
            columnNames = {"testid", "chunk_index"}),
    indexes = {
      @Index(name = "idx_get_status", columnList = "status"),
      @Index(name = "idx_get_testid", columnList = "testid")
    })
@Getter
@Setter
@NoArgsConstructor
public class GetLoadTestMsg extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String projectid;

  @Column(nullable = false)
  private String testid;

  @Column(name = "chunk_index")
  private Integer chunkIndex;

  @Column(name = "total_chunks")
  private Integer totalChunks;

  @Column(columnDefinition = "bytea")
  private byte[] payload;

  @Column(name = "is_complete")
  private Boolean isComplete = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ChunkStatus status = ChunkStatus.RECEIVED;

  @Column(name = "assembled_at")
  private Instant assembledAt;

  @Column(name = "error_msg")
  private String errorMsg;

  public void markAssembled(Instant now) {
    this.status = ChunkStatus.ASSEMBLED;
    this.isComplete = true;
    this.assembledAt = now;
  }
}
