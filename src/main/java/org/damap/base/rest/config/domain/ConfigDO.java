package org.damap.base.rest.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import org.damap.base.domain.ColorTheme;
import org.damap.base.domain.Image;

/** ConfigDO class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDO {

  private String issuer;
  private String clientID;
  private String responseType;
  private String scope;
  private String userRolesClaimPath;
  private String userIdClaim;
  private String nameClaim;
  private String givenNameClaim;
  private String familyNameClaim;
  private String emailClaim;
  private String adminRoleName;
  private String env;
  private List<ServiceConfig> personSearchServiceConfigs;
  private boolean fitsServiceAvailable;
  private boolean livePreviewAvailable;
  private boolean ethicalReportEnabled;
  private String appTitle;
  private ColorTheme colorTheme;
  private List<Image> images;
}
