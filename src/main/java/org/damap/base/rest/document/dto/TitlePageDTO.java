package org.damap.base.rest.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TitlePageDTO {

  private String startdate;
  private String enddate;
  private String funderid;
  private String grantid;
  private String projectid;

  private String projectname;
  private String projectnameText;
  private String acronym;
}
