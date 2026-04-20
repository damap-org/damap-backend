package org.damap.base.integration.pure;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.damap.base.security.SecurityService;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

@ApplicationScoped
class PureAPIFactory {
  @Inject TenantConfigResolver tenantConfigResolver;

  @Inject SecurityService securityService;

  @Inject Instance<FileBasedPureAPI> fileAPI;

  private final Map<String, HTTPBasedPureAPI> httpClients = new ConcurrentHashMap<>();

  @Produces
  @RequestScoped
  @Priority(1)
  PureAPI create() {
    String backend = tenantConfigResolver.getTenantAwareConfig().elsevierPureBackend();
    return switch (backend) {
      case "file" -> fileAPI.get();
      case "http" -> getClient();
      default -> throw new IllegalArgumentException("Pure API backend not supported: " + backend);
    };
  }

  private HTTPBasedPureAPI getClient() {
    String aff = securityService.getAffiliation();
    if (aff == null || tenantConfigResolver.isMultitenancyDisabled()) {
      aff = "no-tenant-registered";
    }
    return httpClients.computeIfAbsent(
        aff,
        missingClient ->
            RestClientBuilder.newBuilder()
                .baseUri(
                    URI.create(
                        tenantConfigResolver.getTenantAwareConfig().elsevierPureEndpointUrl()))
                .register(PureAuthenticationHeaderFactory.class)
                .build(HTTPBasedPureAPI.class));
  }
}
