package org.damap.base.integration.generic;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.integration.ProjectServiceProvider;
import org.damap.base.rda.dmpcommonstandard.ContributorMapper;
import org.damap.base.rda.dmpcommonstandard.ProjectMapper;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.ProjectSupplementDO;
import org.damap.base.security.SecurityService;

@ApplicationScoped
@JBossLog
public class GenericProjectService implements ProjectServiceProvider {
  @Inject GenericCrisClient genericCrisClient;

  private final ProjectMapper projectMapper;
  private final ContributorMapper contributorMapper;
  @Inject SecurityService securityService;

  public GenericProjectService() {
    this(new ProjectMapper(false), new ContributorMapper(false));
  }

  public GenericProjectService(ProjectMapper projectMapper, ContributorMapper contributorMapper) {
    this.projectMapper = projectMapper;
    this.contributorMapper = contributorMapper;
  }

  @Override
  public String getConfigID() {
    return "generic";
  }

  @Override
  @CacheResult(
      cacheName = "generic-cris-project-read",
      keyGenerator = GenericCrisCacheKeyGenerator.class)
  public ProjectDO read(String id) {
    return projectMapper.convert(genericCrisClient.getProject(id));
  }

  @Override
  @CacheResult(
      cacheName = "generic-cris-project-search",
      keyGenerator = GenericCrisCacheKeyGenerator.class)
  public ResultList<ProjectDO> search(Search query) {
    return ResultList.fromItemsAndSearch(
        genericCrisClient.getAllProjects(query.getQuery()).stream()
            .map(projectMapper::convert)
            .toList(),
        query);
  }

  @Override
  @CacheResult(
      cacheName = "generic-cris-project-staff",
      keyGenerator = GenericCrisCacheKeyGenerator.class)
  public List<ContributorDO> getProjectStaff(String projectId) {
    return genericCrisClient.getProjectStaff(projectId).getItems().stream()
        .map(contributorMapper::convert)
        .toList();
  }

  @Override
  @CacheResult(
      cacheName = "generic-cris-project-supplement",
      keyGenerator = GenericCrisCacheKeyGenerator.class)
  public ProjectSupplementDO getProjectSupplement(String projectId) {
    ProjectSupplement supp = genericCrisClient.getProjectSupplements(projectId);

    return ProjectSupplementDO.builder()
        .personalData(supp.getPersonalData())
        .sensitiveData(supp.getSensitiveData())
        .legalRestrictions(supp.getLegalRestrictions())
        .humanParticipants(supp.getHumanParticipants())
        .ethicalIssuesExist(supp.getEthicalIssuesExist())
        .committeeReviewed(supp.getCommitteeReviewed())
        .costsExist(supp.getCostsExist())
        .build();
  }

  @Override
  @CacheResult(
      cacheName = "generic-cris-project-leader",
      keyGenerator = GenericCrisCacheKeyGenerator.class)
  public ContributorDO getProjectLeader(String projectId) {
    return contributorMapper.convert(genericCrisClient.getProjectLeader(projectId));
  }

  @Override
  @CacheResult(
      cacheName = "generic-cris-project-recommended",
      keyGenerator = GenericCrisCacheKeyGenerator.class)
  // Still takes query parameter to be compatible with old method signature - query value is ignored
  public ResultList<ProjectDO> getRecommended(Search query) {

    // This implementation needs to be generic and the only consistent values we have to identify
    // users are the name and email
    // Those two should be enough for implementers to filter for recommended projects
    String email = securityService.getEmail();
    String name = securityService.getDisplayName();
    return ResultList.fromItemsAndSearch(
        genericCrisClient.getRecommendedProjects(email, name).getItems().stream()
            .map(projectMapper::convert)
            .toList(),
        query);
  }
}
