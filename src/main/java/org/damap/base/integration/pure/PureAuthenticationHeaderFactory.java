package org.damap.base.integration.pure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

/** This class implements injecting the API key into the {@link PureAPI}. */
@ApplicationScoped
class PureAuthenticationHeaderFactory implements ClientHeadersFactory {
  @ConfigProperty(name = "damap.elsevier-pure-api-key")
  String apiKey;

  @Override
  public MultivaluedMap<String, String> update(
      MultivaluedMap<String, String> incomingHeaders,
      MultivaluedMap<String, String> clientOutgoingHeaders) {
    MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
    result.add("api-key", apiKey);
    return result;
  }
}
