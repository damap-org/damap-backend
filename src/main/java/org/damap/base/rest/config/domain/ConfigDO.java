package org.damap.base.rest.config.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/** ConfigDO class. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigDO {

  private String authUrl;
  private String authClient;
  private String authScope;
  private String authUser;
  private String env;
  private List<ServiceConfig> personSearchServiceConfigs;
  private boolean fitsServiceAvailable;
  private boolean livePreviewAvailable;
  private String appTitle;

  public void setAppTitle(String appTitle) {
    this.appTitle = appTitle;
  }

  public String getAppTitle() {
    return appTitle;
  }
}
