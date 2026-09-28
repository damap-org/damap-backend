package org.damap.base.rest.openaire.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;

/** SKG-IF controlled type with multilingual labels. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAireType {
  private Map<String, String> labels = new LinkedHashMap<>();
}
