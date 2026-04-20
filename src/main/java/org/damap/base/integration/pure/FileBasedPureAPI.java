package org.damap.base.integration.pure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;

/**
 * This implementation for the {@link PureAPI} reads the data from files instead of a remote
 * endpoint.
 */
@JBossLog
@ApplicationScoped
@Typed(FileBasedPureAPI.class)
@RegisterClientHeaders(PureAuthenticationHeaderFactory.class)
class FileBasedPureAPI implements PureAPI {

  @Inject TenantConfigResolver tenantConfigResolver;

  @Override
  public PureAPIPaginatedProjectsResponse listAllProjects(Long size, Long offset) {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream in =
        tenantConfigResolver.getTenantAwareConfig().elsevierPureProjectsFile().openStream()) {
      return mapper.readValue(in, PureAPIPaginatedProjectsResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PureAPIProject getProject(String uuid) {
    return listAllProjects().stream()
        .filter(p -> p.getUuid().equals(uuid))
        .findFirst()
        .orElse(null);
  }

  @Override
  public PureAPIPaginatedPersonsResponse listAllPersons(Long size, Long offset) {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream in =
        tenantConfigResolver.getTenantAwareConfig().elsevierPurePersonsFile().openStream()) {
      return mapper.readValue(in, PureAPIPaginatedPersonsResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PureAPIPerson getPerson(String uuid) {
    return listAllPersons().stream().filter(p -> p.getUuid().equals(uuid)).findFirst().orElse(null);
  }
}
