package ru.innotech.integration.testing.repository;
import ru.innotech.integration.testing.entity.LoadTestHistory;
import ru.innotech.integration.testing.enums.ReceivedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
public interface LoadTestHistoryRepository extends JpaRepository<LoadTestHistory, Long> {
Optional<LoadTestHistory> findByTestid(String testid);


@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE LoadTestHistory h SET h.totalChunksSent = h.totalChunksSent + 1, h.updatedAt = CURRENT_TIMESTAMP WHERE h.testid = :testid")
int incrementSentChunks(@Param("testid") String testid);


@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("UPDATE LoadTestHistory h SET h.totalChunksReceived = h.totalChunksReceived + 1, h.updatedAt = CURRENT_TIMESTAMP WHERE h.testid = :testid")
int incrementReceivedChunks(@Param("testid") String testid);


List<LoadTestHistory> findByReceivedStatusAndUpdatedAtBefore(ReceivedStatus status, Instant threshold);


}
