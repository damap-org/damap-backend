package org.damap.base.integration.orcid.models.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** ORCIDEmail class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDEmail {
  @JsonProperty String email;

  @JsonProperty boolean verified;

  @JsonProperty boolean primary;
}
