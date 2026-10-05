package org.damap.base.integration.generic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@ApplicationScoped
class ClientHeaderFactory implements ClientHeadersFactory {
  @Inject TenantConfigResolver tenantConfigResolver;

  @Override
  public MultivaluedMap<String, String> update(
      MultivaluedMap<String, String> incomingHeaders,
      MultivaluedMap<String, String> clientOutgoingHeaders) {
    clientOutgoingHeaders.add(
        "Authorization",
        "Bearer "
            + tenantConfigResolver
                .getTenantAwareConfig()
                .genericCrisApiKey()
                .orElse("your-api-key-here"));
    return clientOutgoingHeaders;
  }
}
