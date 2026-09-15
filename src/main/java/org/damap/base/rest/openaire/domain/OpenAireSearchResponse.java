package org.damap.base.rest.openaire.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** Minimal JSON-LD response returned by the OpenAIRE SKG-IF products endpoint. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAireSearchResponse {

  @JsonProperty("@graph")
  private List<OpenAireProduct> graph = new ArrayList<>();
}
