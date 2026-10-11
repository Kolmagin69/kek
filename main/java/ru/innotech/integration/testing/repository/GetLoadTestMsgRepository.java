package ru.innotech.integration.testing.repository;
import ru.innotech.integration.testing.entity.GetLoadTestMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface GetLoadTestMsgRepository extends JpaRepository<GetLoadTestMsg, Long> {
List<GetLoadTestMsg> findByTestidOrderByChunkIndexAsc(String testid);


Optional<GetLoadTestMsg> findByTestidAndChunkIndex(String testid, Integer chunkIndex);


Optional<GetLoadTestMsg> findFirstByTestidOrderByChunkIndexAsc(String testid);


@Query("SELECT g.chunkIndex FROM GetLoadTestMsg g WHERE g.testid = :testid")
List<Integer> findChunkIndexesByTestid(@Param("testid") String testid);


long countByTestidAndIsCompleteFalse(String testid);


}
