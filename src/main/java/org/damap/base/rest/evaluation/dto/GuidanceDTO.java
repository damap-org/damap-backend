package org.damap.base.rest.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/** GuidanceDTO class. Human-readable explanation accompanying an evaluation result. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GuidanceDTO {
  private String summary;
  private List<GuidanceIssueDTO> issues;
}
