package ru.innotech.integration.testing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoadTestRequest {

  @NotBlank private String vertexId;

  @NotBlank private String projectId;

  @NotBlank private String historyProjectId;

  private String createdDate;

  private String updatedDate;

  private String actualDate;

  private String description;

  private Boolean isProject;

  private String testType;

  @NotNull private Map<String, Object> sendTestAttributes = new HashMap<>();
}
