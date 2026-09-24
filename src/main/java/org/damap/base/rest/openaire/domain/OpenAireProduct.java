package org.damap.base.rest.openaire.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/** Minimal SKG-IF product representation used to populate a DAMAP dataset. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAireProduct {

  @JsonProperty("product_type")
  private String productType;

  private Map<String, List<String>> titles = new LinkedHashMap<>();
  private Map<String, List<String>> abstracts = new LinkedHashMap<>();
  private List<OpenAireManifestation> manifestations = new ArrayList<>();
}
