package org.damap.base.rest.openaire.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/** Minimal SKG-IF manifestation dates. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAireDates {
  private List<String> publication = new ArrayList<>();
}
