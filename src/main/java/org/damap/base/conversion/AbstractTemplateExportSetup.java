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

    // determine project leader/coordinator/principal investigator
    projectCoordinators =
        dmp.getContributorList().stream()
            .filter(
                contributor ->
                    contributor.getContributorRole() == EContributorRole.PROJECT_LEADER
                        || contributor.getContributorRole() == EContributorRole.PROJECT_COORDINATOR
                        || contributor.getContributorRole()
                            == EContributorRole.PRINCIPAL_INVESTIGATOR)
            .map(contributor -> ContributorDOMapper.mapEntityToDO(contributor, new ContributorDO()))
            .toList();

    if (projectCoordinators.isEmpty()) {
      try {
        if (dmp.getProject() != null && dmp.getProject().getUniversityId() != null) {
          ContributorDO projectLeader =
              projectService.getProjectLeader(dmp.getProject().getUniversityId());
          if (projectLeader.getRole() == null) {
            projectLeader.setRole(EContributorRole.PROJECT_LEADER);
          }
          projectCoordinators = List.of(projectLeader);
        }
      } catch (Exception e) {
        log.error("Project API not functioning");
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
      List<Contributor> contributors, EContributorRole role) {
    return contributors.stream().filter(c -> c.getContributorRole() == role).toList();
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
          ", ",
          contributors.stream()
              .map(
                  c ->
                      String.format(
                          "%s %s%s",
                          c.getFirstName(),
                          c.getLastName(),
                          c.getMbox() == null ? "" : " (" + c.getMbox() + ")"))
              .toList());
    }
  }
}
