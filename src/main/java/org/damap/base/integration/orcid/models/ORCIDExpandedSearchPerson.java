package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import lombok.Data;

/** ORCIDExpandedSearchPerson class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDExpandedSearchPerson {

  @JsonProperty(value = "orcid-id")
  String orcidId;

  @JsonProperty(value = "given-names")
  String givenNames;

  @JsonProperty(value = "family-names")
  String familyNames;

  @JsonProperty(value = "email")
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  List<String> emails = List.of();

  @JsonProperty(value = "institution-name")
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  List<String> affiliations = List.of();
}
