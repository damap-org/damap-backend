package org.damap.base.rest.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegalEthicalInformationDTO {

  private String personaldata;
  private String legalrestriction;
  private String ethicalissues;
}
