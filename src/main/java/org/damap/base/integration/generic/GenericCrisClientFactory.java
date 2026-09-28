package org.damap.base.integration.generic;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.damap.base.security.SecurityService;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@ApplicationScoped
public class GenericCrisClientFactory {
  @Inject TenantConfigResolver tenantConfigResolver;

  @Inject SecurityService securityService;

  private final Map<String, GenericCrisClient> clients = new ConcurrentHashMap<>();

  /**
   * Returns a cached {@link GenericCrisClient} client for the current tenant.
   *
   * <p>If no client exists for the resolved tenant key, a new REST client is created using the
   * configured evaluation service URL.
   *
   * <p>The client needs to be created dynamically for multitenancy, since different clients need to
   * be chosen at runtime, depending on which tenant tries to use the client.
   *
   * @return a tenant-specific {@link GenericCrisClient} client
   */
  @Produces
  @RequestScoped
  @Priority(1)
  GenericCrisClient create() {
    String aff = securityService.getAffiliation();
    if (aff == null || tenantConfigResolver.isMultitenancyDisabled()) {
      aff = "no-tenant-registered";
    }
    return clients.computeIfAbsent(
        aff,
        missingClient ->
            RestClientBuilder.newBuilder()
                .baseUri(URI.create(tenantConfigResolver.getTenantAwareConfig().genericCrisUrl()))
                .build(GenericCrisClient.class));
  }
}
