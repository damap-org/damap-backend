package org.damap.base.rest.dmp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/** Damap compatible representation of additional project information */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectSupplementDO {

  private Boolean personalData;
  private Boolean sensitiveData;
  private Boolean legalRestrictions;
  private Boolean humanParticipants;
  private Boolean ethicalIssuesExist;
  private Boolean committeeReviewed;
  private Boolean costsExist;
}
