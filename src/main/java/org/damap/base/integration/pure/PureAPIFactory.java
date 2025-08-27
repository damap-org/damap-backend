package org.damap.base.integration.pure;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
class PureAPIFactory {
  @ConfigProperty(name = "damap.elsevier-pure-backend", defaultValue = "http")
  String backend;

  @Inject Instance<FileBasedPureAPI> fileAPI;

  @Inject @RestClient Instance<HTTPBasedPureAPI> httpAPI;

  @Produces
  @Priority(1)
  PureAPI create() {
    return switch (backend) {
      case "file" -> fileAPI.get();
      case "http" -> httpAPI.get();
      default -> throw new IllegalArgumentException("Pure API backend not supported: " + backend);
    };
  }
}
