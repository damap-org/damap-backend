package org.damap.base.rest.evaluation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

// TODO: Check if there are any OpenApi or Json specifications so we can autogenerate this by
// pulling the specification
/** BenchmarkDTO class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BenchmarkDTO {
  private String identifier;
  private String title;
  private String description;
  private String version;
  private List<String> hasAssociatedMetric;
  private List<String> scoringFunction;
  private String keyword;
  private String abbreviation;
  private String landingPage;
  private String theme;
  private String status;
  private List<String> creator;
  private String license;
}
