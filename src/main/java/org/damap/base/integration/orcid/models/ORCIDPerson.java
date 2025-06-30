package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** ORCIDPerson class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDPerson {

  @JsonProperty(value = "name")
  ORCIDPersonName name;

  @JsonProperty ORCIDEmails emails;
}
