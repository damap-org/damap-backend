package org.damap.base.rest.config.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ServiceConfigDO class. Since the ServiceConfig runtime interface proxy cannot be marshalled and
 * sent to the frontend, we need this DO
 */
@Data
@AllArgsConstructor
public class ServiceConfigDO {

  String displayText;

  String queryValue;

  String className;

  public ServiceConfigDO(DamapTenantAwareConfig.ServiceConfig serviceConfig) {
    this.displayText = serviceConfig.displayText();
    this.queryValue = serviceConfig.queryValue();
    this.className = serviceConfig.className();
  }
}
