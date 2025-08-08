package org.damap.base.integration.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.enums.EContributorRole;
import org.damap.base.integration.ProjectServiceProvider;
import org.damap.base.rda.dmpcommonstandard.ContributorMapper;
import org.damap.base.rda.dmpcommonstandard.ProjectMapper;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.ProjectSupplementDO;

@ApplicationScoped
@JBossLog
public class RESTProjectService implements ProjectServiceProvider {
  // TODO add caching
  @Inject Client client;

  private final ProjectMapper projectMapper;
  private final ContributorMapper contributorMapper;

  public RESTProjectService() {
    this(new ProjectMapper(false), new ContributorMapper(false));
  }

  public RESTProjectService(ProjectMapper projectMapper, ContributorMapper contributorMapper) {
    this.projectMapper = projectMapper;
    this.contributorMapper = contributorMapper;
  }

  @Override
  public String getConfigID() {
    return "REST";
  }

  @Override
  public ProjectDO read(String id) {
    return projectMapper.convert(client.getProject(id));
  }

  @Override
  public ResultList<ProjectDO> search(Search query) {
    // TODO support client-side pagination here to avoid DoS-ing the backing service?
    return ResultList.fromItemsAndSearch(
        client.listAllProjects(query.getQuery()).stream().map(projectMapper::convert).toList(),
        query);
  }

  @Override
  public List<ContributorDO> getProjectStaff(String projectId) {
    // TODO support client-side pagination here to avoid DoS-ing the backing service?
    return client.listAllProjectContributors(projectId, null).stream()
        .map(contributorMapper::convert)
        .toList();
  }

  @Override
  public ProjectSupplementDO getProjectSupplement(String projectId) {
    // Run this query to catch 404s:
    client.getProject(projectId);

    // We can't currently map this from a Project object.
    return null;
  }

  @Override
  public ContributorDO getProjectLeader(String projectId) {
    // TODO optimize this to avoid fetching all contributors if it becomes a performance problem:
    return client.listAllProjectContributors(projectId, null).stream()
        .map(contributorMapper::convert)
        .filter(contributor -> contributor.getRoles().contains(EContributorRole.PROJECT_LEADER))
        .findFirst()
        .orElse(null);
  }
}
