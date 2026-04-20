package org.damap.base.rest;

import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.damap.base.integration.ProjectServiceProvider;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.ProjectSupplementDO;

/**
 * This class is an overlay for {@link ProjectServiceProvider} that automatically determines which
 * configured instance is to be used. Ideally, this would be solved by CDI, but CDI doesn't want to
 * play ball when it comes to dynamically selecting instances. Feel free to refactor if you like
 * pain.
 */
@Slf4j
@ApplicationScoped
@Typed(ProjectService.class)
public final class ProjectService implements ProjectServiceProvider {
  @Inject @All List<ProjectServiceProvider> services;

  @Inject TenantConfigResolver tenantConfigResolver;

  ProjectServiceProvider backingServiceCache;

  public ProjectService() {}

  public ProjectService(List<ProjectServiceProvider> services) {
    this.services = services;
  }

  private ProjectServiceProvider getProjectService() {
    String selectedService = tenantConfigResolver.getTenantAwareConfig().projectService();
    log.info("Getting projects service " + selectedService);
    if (backingServiceCache == null) {
      if (services.isEmpty()) {
        throw new IllegalArgumentException(
            "Bug: no ProjectServiceProvider implementations are available.");
      }

      backingServiceCache =
          services.stream()
              .filter(
                  svc -> {
                    String configId = svc.getConfigID();
                    if (configId == null && selectedService.equals("default")) {
                      // This is a workaround for the MockProjectService test implementation because
                      // the mocks don't
                      // implement this function correctly.
                      return true;
                    }
                    return Objects.equals(configId, selectedService);
                  })
              .findFirst()
              .orElse(null);

      if (backingServiceCache == null) {
        Set<String> serviceIDs =
            services.stream()
                .map(ProjectServiceProvider::getConfigID)
                .map(configId -> configId == null ? "default" : configId)
                .collect(Collectors.toSet());
        throw new RuntimeException(
            "The configured damap.tenant-aware.project-service ("
                + selectedService
                + ") was not found. "
                + "Please select one of "
                + String.join(", ", serviceIDs));
      }
    }
    return backingServiceCache;
  }

  /** {@inheritDoc} */
  @Override
  public List<ContributorDO> getProjectStaff(String projectId) {
    return getProjectService().getProjectStaff(projectId);
  }

  /** {@inheritDoc} */
  @Override
  public ProjectSupplementDO getProjectSupplement(String projectId) {
    return getProjectService().getProjectSupplement(projectId);
  }

  /** {@inheritDoc} */
  @Override
  public ContributorDO getProjectLeader(String projectId) {
    return getProjectService().getProjectLeader(projectId);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ProjectDO> getRecommended(Search search) {
    return getProjectService().getRecommended(search);
  }

  /** {@inheritDoc} */
  @Override
  public ProjectDO read(String id) {
    return getProjectService().read(id);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ProjectDO> search(Search query) {
    return getProjectService().search(query);
  }
}
