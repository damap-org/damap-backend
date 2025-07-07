package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import lombok.Data;

/** ORCIDExpandedSearchResult class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDExpandedSearchResult {

  @JsonProperty(value = "expanded-result")
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  private List<ORCIDExpandedSearchPerson> persons;

  @JsonProperty(value = "num-found")
  private long numFound;
}
