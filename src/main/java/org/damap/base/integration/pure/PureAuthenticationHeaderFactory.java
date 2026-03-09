package org.damap.base.integration.pure;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

/** This class implements injecting the API key into the {@link PureAPI}. */
@ApplicationScoped
@Unremovable
public class PureAuthenticationHeaderFactory implements ClientHeadersFactory {

  @Inject TenantConfigResolver tenantConfigResolver;

  @Override
  public MultivaluedMap<String, String> update(
      MultivaluedMap<String, String> incomingHeaders,
      MultivaluedMap<String, String> clientOutgoingHeaders) {
    MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
    result.add("api-key", tenantConfigResolver.getTenantAwareConfig().elsevierPureApiKey());
    return result;
  }
}
