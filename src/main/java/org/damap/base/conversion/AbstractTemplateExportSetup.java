package org.damap.base.conversion;

import jakarta.inject.Inject;
import java.util.*;
import lombok.extern.jbosslog.JBossLog;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.damap.base.domain.Contributor;
import org.damap.base.domain.Cost;
import org.damap.base.domain.Dataset;
import org.damap.base.domain.Dmp;
import org.damap.base.enums.EContributorRole;
import org.damap.base.r3data.RepositoriesService;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.mapper.ContributorDOMapper;
import org.damap.base.rest.projects.ProjectService;

/** This class describes necessary setup for all template export classes. */
@JBossLog
public abstract class AbstractTemplateExportSetup extends AbstractTemplateExportFunctions {

  @Inject RepositoriesService repositoriesService;

  @Inject ProjectService projectService;

  @Inject TemplateFileBrokerService templateFileBrokerService;

  @Inject LoadResourceService loadResourceService;

  protected Map<String, String> replacements = new HashMap<>();
  protected Map<String, String> footerMap = new HashMap<>();

  // Convert the date for readable format for the document
  protected Dmp dmp = null;
  protected List<Dataset> datasets = null;
  protected List<Dataset> deletedDatasets = null;
  protected List<Cost> costList = null;

  // elements of the document that need to be navigated through
  protected Properties prop = null;
  protected List<XWPFParagraph> xwpfParagraphs = null;
  protected List<XWPFTable> xwpfTables = null;
  protected List<ContributorDO> projectCoordinators = new ArrayList<>();

  /**
   * exportSetup.
   *
   * @param dmpId a long
   */
  protected void exportSetup(long dmpId) {
    // Loading data related to the project from database
    dmp = dmpRepo.findById(dmpId);
    datasets = dmp.getDatasetList();
    deletedDatasets = getDeletedDatasets(datasets);
    costList = dmp.getCosts();

    // Determine project leader/coordinator/principal investigator
    projectCoordinators =
        dmp.getContributorList().stream()
            .filter(
                contributor -> {
                  Set<EContributorRole> roles = contributor.getContributorRoles();
                  return roles != null
                      && roles.stream().anyMatch(EContributorRole::isLeadershipRole);
                })
            .map(contributor -> ContributorDOMapper.mapEntityToDO(contributor, new ContributorDO()))
            .toList();

    if (projectCoordinators.isEmpty()) {
      try {
        if (dmp.getProjectUniversityId() != null) {
          ContributorDO projectLeader =
              projectService.getProjectLeader(dmp.getProjectUniversityId());

          if (projectLeader != null) {
            if (projectLeader.getRoles() == null || projectLeader.getRoles().isEmpty()) {
              projectLeader.setRoles(new HashSet<>(Set.of(EContributorRole.PROJECT_LEADER)));
            }
            projectCoordinators = List.of(projectLeader);
          } else {
            log.warn("Project Leader is null for project ID: " + dmp.getProjectUniversityId());
          }
        }
      } catch (Exception e) {
        log.error("Project API not functioning", e);
      }
    }
  }

  private List<Dataset> getDeletedDatasets(List<Dataset> datasets) {
    return datasets.stream().filter(Dataset::getDelete).toList();
  }

  /**
   * getContributorsByRole.
   *
   * @param contributors a {@link java.util.List} object
   * @param role a {@link org.damap.base.enums.EContributorRole} object
   * @return a {@link java.util.List} object
   */
  protected List<Contributor> getContributorsByRole(
      List<Contributor> contributors, Set<EContributorRole> roles) {
    return contributors.stream()
        .filter(
            c ->
                c.getContributorRoles() != null
                    && !Collections.disjoint(c.getContributorRoles(), roles))
        .toList();
  }

  /**
   * getContributorsText.
   *
   * @param contributors a {@link java.util.List} object
   * @return a {@link java.lang.String} object
   */
  protected String getContributorsText(List<Contributor> contributors) {
    if (contributors.isEmpty()) {
      return "";
    } else {
      return String.join(
          "; ",
          contributors.stream()
              .map(
                  c ->
                      String.format(
                          "%s %s%s [%s]",
                          c.getFirstName(),
                          c.getLastName(),
                          c.getMbox() == null ? "" : " (" + c.getMbox() + ")",
                          c.getContributorRoles() != null
                              ? String.join(
                                  ", ",
                                  c.getContributorRoles().stream()
                                      .map(EContributorRole::getRoles)
                                      .toList())
                              : ""))
              .toList());
    }
  }
}
