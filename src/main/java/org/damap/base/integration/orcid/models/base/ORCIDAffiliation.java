package org.damap.base.integration.orcid.models.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.damap.base.integration.orcid.models.ORCIDOrganization;

/** ORCIDAffiliation class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ORCIDAffiliation {
  @JsonProperty(value = "department-name")
  String departmentName;

  @JsonProperty(value = "role-title")
  String roleTitle;

  @JsonProperty(value = "start-date")
  ORCIDDate startDate;

  @JsonProperty(value = "end-date")
  ORCIDDate endDate;

  @JsonProperty ORCIDOrganization organization;
}
