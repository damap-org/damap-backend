package org.damap.base.rest.openaire.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Minimal SKG-IF access-rights representation. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAireAccessRights {
  private String status;
}
