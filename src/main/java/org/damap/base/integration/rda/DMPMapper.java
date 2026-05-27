package org.damap.base.integration.rda;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.damap.base.integration.rest.*;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.ProjectDO;

/**
 * This class implements DMP conversion from and to the RDA DMP common standard. (See <a
 * href="https://github.com/RDA-DMP-Common/common-madmp-api">github.com/RDA-DMP-Common/common-madmp-api</a>
 * )
 *
 * <p>The conversion from the common standard into DAMAP objects is best-effort since not all data
 * can be represented.
 */
public final class DMPMapper extends AbstractMapper {

  private final ProjectMapper projectMapper;
  private final ContributorMapper contributorMapper;
  private final CostsMapper costsMapper;
  private final DatasetMapper datasetMapper;

  /** Initialize the mapper with the default settings (strict mode turned on). */
  public DMPMapper() {
    this(true);
  }

  /**
   * Initialize the mapper with an option to turn off strict mode. Only use non-strict mode when a
   * human has to review the DMP afterward, never use it for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   */
  public DMPMapper(boolean strict) {
    this(
        strict,
        new ProjectMapper(strict),
        new ContributorMapper(strict),
        new CostsMapper(strict),
        new DatasetMapper(strict));
  }

  /**
   * Initialize the mapper with an option to turn off strict mode and the ability to override
   * submappers. Only use non-strict mode when a human has to review the DMP afterward, never use it
   * for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   * @param projectMapper contains the pre-configured {@link ProjectMapper}.
   * @param costsMapper contains a pre-configured {@link CostsMapper}.
   * @param datasetMapper contains a pre-configured {@link DatasetMapper}.
   */
  public DMPMapper(
      boolean strict,
      ProjectMapper projectMapper,
      ContributorMapper contributorMapper,
      CostsMapper costsMapper,
      DatasetMapper datasetMapper) {
    super(strict);
    this.projectMapper = projectMapper;
    this.contributorMapper = contributorMapper;
    this.costsMapper = costsMapper;
    this.datasetMapper = datasetMapper;
  }

  public DMPWithID convert(DmpDO dmp) {
    return new DMPWithID().id(String.valueOf(dmp.getId())).dmp(convertData(dmp));
  }

  public DmpDO convert(DMPWithID dmp) {
    var dmpDO = new DmpDO();
    dmpDO.setId(Long.valueOf(dmp.getId()));
    var dmpData = dmp.getDmp();
    if (dmpData == null) {
      return dmpDO;
    }
    convertData(dmpData, dmpDO);
    return dmpDO;
  }

  private DMPData convertData(DmpDO dmp) {
    DMPData result = new DMPData();
    result.setCreated(OffsetDateTime.from(dmp.getCreated().toInstant()));
    result.setModified(OffsetDateTime.from(dmp.getModified().toInstant()));
    result.setLanguage(LanguageID.ENG);
    ProjectDO project = dmp.getProject();
    if (project != null) {
      result.setDmpId(new DMPID().type(DMPIDType.OTHER).identifier(project.getUniversityId()));
      result.dmpId(
          new DMPID().identifier(dmp.getProject().getUniversityId()).type(DMPIDType.OTHER));

      result.project(List.of(projectMapper.convert(project)));
    }
    result.setContact(contributorMapper.convertToContact(dmp.getContact()));
    var contributors = dmp.getContributors();
    if (contributors != null) {
      result.setContributor(
          contributors.stream().map(contributorMapper::convert).collect(Collectors.toList()));
    }
    var costs = dmp.getCosts();
    if (costs != null) {
      result.setCost(costs.stream().map(costsMapper::convert).collect(Collectors.toList()));
    }

    result.setDataset(dmp.getDatasets().stream().map(datasetMapper::convert).toList());

    var ethicalIssuesExist = dmp.getEthicalIssuesExist();
    if (ethicalIssuesExist != null) {
      if (ethicalIssuesExist) {
        result.setEthicalIssuesExist(Booleanish.YES);
      } else {
        result.setEthicalIssuesExist(Booleanish.NO);
      }
    } else {
      result.setEthicalIssuesExist(Booleanish.UNKNOWN);
    }
    result.setEthicalIssuesReport(dmp.getEthicalIssuesReport());
    return result;
  }

  private void convertData(DMPData data, DmpDO target) {
    var projects = data.getProject();
    if (projects != null) {
      if (projects.size() > 1 && strict) {
        throw new CommonStandardCompatibilityException("more than one project present");
      }
      for (var project : projects) {
        target.setProject(projectMapper.convert(project, data.getDmpId().getIdentifier()));
      }
    }
    var contributors = data.getContributor();
    if (contributors != null) {
      target.setContributors(
          contributors.stream().map(contributorMapper::convert).collect(Collectors.toList()));
    }
    if (data.getLanguage() != LanguageID.ENG && strict) {
      throw new CommonStandardCompatibilityException(
          "DAMAP does not support importing non-English DMPs");
    }
    var costs = data.getCost();
    if (costs != null) {
      target.setCosts(costs.stream().map(costsMapper::convert).toList());
    }
    target.setDatasets(data.getDataset().stream().map(datasetMapper::convert).toList());
    if (data.getEthicalIssuesDescription() != null
        && !data.getEthicalIssuesDescription().isEmpty()
        && strict) {
      throw new CommonStandardCompatibilityException(
          "DAMAP does not support the ethical_issues_description field");
    }
    target.setEthicalIssuesExist(
        switch (data.getEthicalIssuesExist()) {
          case YES -> true;
          case NO -> false;
          case UNKNOWN -> null;
        });
    target.setEthicalIssuesReport(data.getEthicalIssuesReport());
    if (data.getEthicalIssuesDescription() != null
        && !data.getEthicalIssuesDescription().isEmpty()
        && strict) {
      throw new CommonStandardCompatibilityException(
          "DAMAP does not support the ethical_issues_description field");
    }
  }
}
