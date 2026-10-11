package ru.innotech.integration.testing.repository;

import com.arangodb.springframework.repository.ArangoRepository;
import ru.innotech.integration.testing.entity.LoadTestingDocument;

public interface LoadTestingRepository extends ArangoRepository<LoadTestingDocument, String> {
}
