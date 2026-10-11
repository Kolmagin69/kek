package ru.innotech.integration.testing.entity;

import ru.innotech.integration.testing.enums.OverallStatus;
import ru.innotech.integration.testing.enums.ReceivedStatus;
import ru.innotech.integration.testing.enums.SentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(
    name = "load_test_history",
    indexes = {
      @Index(name = "idx_hist_testid", columnList = "testid"),
      @Index(name = "idx_hist_status", columnList = "overall_status")
    })
@Getter
@Setter
@NoArgsConstructor
public class LoadTestHistory extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String projectid;
  private String testid;
  private String userid;

  @Column(name = "total_chunks_sent")
  private Integer totalChunksSent = 0;

  @Column(name = "total_chunks_received")
  private Integer totalChunksReceived = 0;

  @Enumerated(EnumType.STRING)
  @Column(name = "sent_status", length = 20)
  private SentStatus sentStatus = SentStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(name = "received_status", length = 20)
  private ReceivedStatus receivedStatus = ReceivedStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(name = "overall_status", length = 20)
  private OverallStatus overallStatus = OverallStatus.INITIATED;

  @Column(name = "msg_size")
  private Long msgSize;

  @Column(name = "sent_payload_preview")
  private String sentPayloadPreview;

  @Column(name = "received_payload_preview")
  private String receivedPayloadPreview;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "received_at")
  private Instant receivedAt;

  @Column(name = "assembled_at")
  private Instant assembledAt;

  @Column(name = "error_msg")
  private String errorMsg;

  public void startSending() {
    this.overallStatus = OverallStatus.SENDING;
  }

  public void markReceiving() {
    this.receivedStatus = ReceivedStatus.RECEIVING;
    this.overallStatus = OverallStatus.RECEIVING;
  }

  public void markSentComplete(long msgSize, String preview, Instant now) {
    this.sentStatus = SentStatus.SENT;
    this.overallStatus = OverallStatus.SENT;
    this.sentAt = now;
    this.msgSize = msgSize;
    this.sentPayloadPreview = preview;
  }

  public void markSentFailed(String error) {
    this.sentStatus = SentStatus.FAILED;
    this.overallStatus = OverallStatus.FAILED;
    this.errorMsg = error;
  }

  public void markReceiveComplete(String preview, Instant now) {
    this.receivedStatus = ReceivedStatus.ASSEMBLED;
    this.overallStatus = OverallStatus.COMPLETED;
    this.receivedAt = now;
    this.assembledAt = now;
    this.receivedPayloadPreview = preview;
  }

  public void markReceiveFailed(String error) {
    this.receivedStatus = ReceivedStatus.FAILED;
    this.overallStatus = OverallStatus.FAILED;
    this.errorMsg = error;
  }
}
