package org.damap.base.rest.config.domain;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.damap.base.security.SecurityService;

@ApplicationScoped
public class TenantConfigResolver {

  @Inject DamapSingleTenantConfig defaultConfig;
  @Inject DamapMultiTenantConfig multiTenantConfig;

  @Inject SecurityService securityService;

  public DamapTenantAwareConfig getTenantAwareConfig() {
    // catches case of user logging into multitenant setup without a valid affiliation to check out
    // the system
    // would throw 401 otherwise - and those users shouldnt get their information stored
    if (!securityService.doesUserHaveValidAffiliation()) {
      return defaultConfig;
    }
    String aff = securityService.getAffiliation();
    if (aff == null || isMultitenancyDisabled()) {
      return defaultConfig;
    } else {
      return multiTenantConfig.tenantConfigs().get(aff);
    }
  }

  public boolean isMultitenancyDisabled() {
    List<String> tenants = multiTenantConfig.tenantList().orElse(null);
    return tenants == null || tenants.isEmpty();
  }
}
