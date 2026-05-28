package org.damap.base.integration.generic;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@ApplicationScoped
class ClientHeaderFactory implements ClientHeadersFactory {

  @ConfigProperty(name = "damap.generic-cris-api-key", defaultValue = "")
  String apikey;

  @Override
  public MultivaluedMap<String, String> update(
      MultivaluedMap<String, String> incomingHeaders,
      MultivaluedMap<String, String> clientOutgoingHeaders) {
    clientOutgoingHeaders.add("Authorization", "Bearer " + apikey);
    return clientOutgoingHeaders;
  }
}
