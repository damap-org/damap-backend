package org.damap.base.rest.openaire.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Minimal SKG-IF manifestation representation. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAireManifestation {

  private OpenAireType type;
  private OpenAireDates dates;
  private String licence;

  @JsonProperty("access_rights")
  private OpenAireAccessRights accessRights;
}
