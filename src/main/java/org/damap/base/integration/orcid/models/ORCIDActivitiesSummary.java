package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** ORCIDActivitiesSummary class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDActivitiesSummary {
  @JsonProperty ORCIDAffiliationGroup<ORCIDEmploymentSummary> employments;

  @JsonProperty ORCIDAffiliationGroup<ORCIDEducationSummary> educations;
}
