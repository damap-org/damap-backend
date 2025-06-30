package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.damap.base.integration.orcid.models.base.ORCIDAffiliation;

/** ORCIDEducationSummary class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDEducationSummary implements ORCIDAffiliationSummary {

  @JsonSetter(value = "education-summary")
  ORCIDAffiliation summary;
}
