package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** ORCIDRecord class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDRecord {
  @JsonProperty ORCIDPerson person;

  @JsonProperty(value = "activities-summary")
  ORCIDActivitiesSummary activitiesSummary;
}
