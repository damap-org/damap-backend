package org.damap.base.rest.config.domain;

import io.smallrye.config.ConfigMapping;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holds a map of all configuration options which need to be configured per tenant. In case of
 * single tenant mode, this is empty and unused. In case of multitenancy enabled, this configuration
 * object can be used to fetch correct configuration dependent on the current tenant and to check if
 * multitenancy is active using tenants().
 */
@ConfigMapping(prefix = "damap.tenants")
public interface DamapMultiTenantConfig {
  Optional<List<String>> tenants();

  Map<String, DamapTenantAwareConfig> tenantConfigs();
}
