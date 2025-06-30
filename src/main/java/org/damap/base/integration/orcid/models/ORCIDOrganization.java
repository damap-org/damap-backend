package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** ORCIDOrganization class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDOrganization {
  @JsonProperty String name;
}
