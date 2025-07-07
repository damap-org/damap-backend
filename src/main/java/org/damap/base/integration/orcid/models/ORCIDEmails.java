package org.damap.base.integration.orcid.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import lombok.Data;
import org.damap.base.integration.orcid.models.base.ORCIDEmail;

/** ORCIDEmails class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDEmails {
  @JsonProperty
  @JsonSetter(nulls = Nulls.AS_EMPTY)
  List<ORCIDEmail> email = List.of();
}
