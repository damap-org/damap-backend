package org.damap.base.integration.pure;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.lookup.LookupIfProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This implementation for the {@link PureAPI} reads the data from files instead of a remote
 * endpoint.
 */
@JBossLog
@ApplicationScoped
@Typed(FileBasedPureAPI.class)
@LookupIfProperty(name = "damap.elsevier-pure-backend", stringValue = "file")
class FileBasedPureAPI implements PureAPI {
  @ConfigProperty(name = "damap.elsevier-pure-projects-file")
  URL projectsFile;

  @ConfigProperty(name = "damap.elsevier-pure-persons-file")
  URL personsFile;

  @Override
  public PureAPIPaginatedProjectsResponse listAllProjects(Long size, Long offset) {
    ObjectMapper mapper = new ObjectMapper();
    try (InputStream in = projectsFile.openStream()) {
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
    try (InputStream in = personsFile.openStream()) {
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
