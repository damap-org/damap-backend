package org.damap.base.rest.evaluation;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.damap.base.security.SecurityService;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

public class EvaluationRemoteResourceFactory {
  @Inject TenantConfigResolver tenantConfigResolver;

  @Inject SecurityService securityService;

  private final Map<String, EvaluationRemoteResource> clients = new ConcurrentHashMap<>();

  /**
   * Returns a cached {@link EvaluationRemoteResource} client for the current tenant.
   *
   * <p>If no client exists for the resolved tenant key, a new REST client is created using the
   * configured evaluation service URL.
   *
   * <p>The client needs to be created dynamically for multitenancy, since different clients need to
   * be chosen at runtime, depending on which tenant tries to use the client.
   *
   * @return a tenant-specific {@link EvaluationRemoteResource} client
   */
  @Produces
  @RequestScoped
  @Priority(1)
  EvaluationRemoteResource create() {
    String aff = securityService.getAffiliation();
    if (aff == null || tenantConfigResolver.isMultitenancyDisabled()) {
      aff = "no-tenant-registered";
    }
    return clients.computeIfAbsent(
        aff,
        missingClient ->
            RestClientBuilder.newBuilder()
                .baseUri(
                    URI.create(
                        tenantConfigResolver
                            .getTenantAwareConfig()
                            .evaluationServiceUrl()
                            .orElse("")))
                .build(EvaluationRemoteResource.class));
  }
}
