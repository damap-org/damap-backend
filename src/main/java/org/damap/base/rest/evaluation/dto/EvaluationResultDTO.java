package org.damap.base.rest.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

// TODO: Check if there are any OpenApi or Json specifications so we can autogenerate this by
// pulling the specification
/** EvaluationResultDTO class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvaluationResultDTO {
  private String identifier;
  private String title;
  private String description;
  private String value;
  private String generatedAtTime;
  private String reportId;
  private String log;
  private List<String> affectedElements;
  private Integer completion;
  private String assessmentTarget;
  private String wasGeneratedBy;
  private String outputFromTest;
}
