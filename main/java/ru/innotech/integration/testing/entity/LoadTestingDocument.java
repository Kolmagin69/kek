package ru.innotech.integration.testing.entity;

import com.arangodb.springframework.annotation.Document;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Document("load_testing")
public class LoadTestingDocument {

  @Id private String id;

  private String vertexId;
  private String projectId;
  private String historyProjectId;
  private String createdDate;
  private String updatedDate;
  private String actualDate;
  private String description;
  private Boolean isProject;
  private String userUuid;
  private String userName;
  private String testType;

  private Map<String, Object> sendTestAttributes = new HashMap<>();
  private Map<String, Object> getTestAttributes = new HashMap<>();

  public void mergeGetResult(Map<String, Object> result) {
    if (getTestAttributes == null) getTestAttributes = new HashMap<>();
    getTestAttributes.putAll(result);
  }
}
