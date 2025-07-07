package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import lombok.Data;

/** ORCIDGroup class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDGroup<T extends ORCIDAffiliationSummary> {
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  List<T> summaries = List.of();
}
