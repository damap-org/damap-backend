package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.damap.base.integration.orcid.models.base.ORCIDAffiliation;

/** ORCIDEmploymentSummary class. */
@Data
public class ORCIDEmploymentSummary implements ORCIDAffiliationSummary {
  @JsonProperty(value = "employment-summary")
  ORCIDAffiliation summary;
}
