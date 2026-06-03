package org.damap.base.rest.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** GuidanceIssueDTO class. A single dataset-level issue within an evaluation guidance. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuidanceIssueDTO {
  private String dataset;
  private String reason;
}
