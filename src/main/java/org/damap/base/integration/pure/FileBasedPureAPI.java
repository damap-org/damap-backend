package org.damap.base.integration.pure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject TenantConfigResolver tenantConfigResolver;

  @Override
  public PureAPIPaginatedProjectsResponse listAllProjects(Long size, Long offset) {
    return singlePage(readAllProjectsFromFile());
  }

  @Override
  public PureAPIPaginatedProjectsResponse searchProjects(String q, Long size, Long offset) {
    List<PureAPIProject> all = readAllProjectsFromFile();
    if (q != null && !q.isBlank()) {
      all = all.stream().filter(p -> p != null && p.titleContains(q)).toList();
    }
    return singlePage(all);
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
    try (InputStream in =
        tenantConfigResolver.getTenantAwareConfig().elsevierPurePersonsFile().openStream()) {
      return objectMapper.readValue(in, PureAPIPaginatedPersonsResponse.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public PureAPIPerson getPerson(String uuid) {
    return listAllPersons().stream().filter(p -> p.getUuid().equals(uuid)).findFirst().orElse(null);
  }

  private List<PureAPIProject> readAllProjectsFromFile() {
    try (InputStream in =
        tenantConfigResolver.getTenantAwareConfig().elsevierPureProjectsFile().openStream()) {
      PureAPIPaginatedProjectsResponse resp =
          objectMapper.readValue(in, PureAPIPaginatedProjectsResponse.class);
      if (resp == null || resp.getItems() == null) {
        return List.of();
      }
      return resp.getItems();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private PureAPIPaginatedProjectsResponse singlePage(List<PureAPIProject> all) {
    PureAPIPaginatedProjectsResponse page = new PureAPIPaginatedProjectsResponse();
    page.setCount(all.size());

    PureAPIPageInformation info = new PureAPIPageInformation();
    info.setOffset(0);
    info.setSize(all.size());
    page.setPageInformation(info);

    page.setItems(new ArrayList<>(all));
    return page;
  }
}
