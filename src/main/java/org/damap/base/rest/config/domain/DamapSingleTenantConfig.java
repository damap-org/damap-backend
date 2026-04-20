package org.damap.base.rest.config.domain;

import io.smallrye.config.ConfigMapping;

/**
 * Holds all configuration options which need to be configured per tenant. In case of multitenancy,
 * this is unused. In single tenant mode, it acts as a normal configuration.
 */
@ConfigMapping(prefix = "damap.tenant-aware")
public interface DamapSingleTenantConfig extends DamapTenantAwareConfig {}
