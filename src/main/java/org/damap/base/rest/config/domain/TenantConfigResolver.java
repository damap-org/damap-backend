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
    String aff = securityService.getAffiliation();
    if (aff == null || isMultitenancyDisabled()) {
      return defaultConfig;
    } else {
      return multiTenantConfig.tenantConfigs().get(aff);
    }
  }

  public boolean isMultitenancyDisabled() {
    List<String> tenants = multiTenantConfig.tenants().orElse(null);
    return tenants == null || tenants.isEmpty();
  }
}
