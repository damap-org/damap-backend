package org.damap.base.rda.dmpcommonstandard;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.damap.base.integration.generic.DAMAPProject;
import org.damap.base.rest.dmp.domain.ProjectDO;

/**
 * This class implements Project conversion from and to the RDA DMP common standard. (See <a
 * href="https://github.com/RDA-DMP-Common/common-madmp-api">github.com/RDA-DMP-Common/common-madmp-api</a>
 * )
 *
 * <p>The conversion from the common standard into DAMAP objects is best-effort since not all data
 * can be represented.
 */
public final class ProjectMapper {
  /**
   * Tolerate compatibility failures with the common standard and drop non-standard data. This flag
   * should never be used when importing DMP data, only for integrations.
   */
  private final boolean strict;

  private final FundingMapper fundingMapper;

  /** Initialize the mapper with the default configuration (strict mode turned on). */
  public ProjectMapper() {
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
  public ProjectMapper(boolean strict) {
    this(strict, new FundingMapper(strict));
  }

  /**
   * Initialize the mapper with an option to turn off strict mode and the ability to override the
   * Funding mapper. Only use non-strict mode when a human has to review the DMP afterward, never
   * use it for automated imports!
   *
   * @param strict When enabled, any dropped data will cause a {@link
   *     CommonStandardCompatibilityException}. When disabled, DAMAP will do its best to represent
   *     any imported data in the common format but drop data it cannot represent.
   * @param fundingMapper contains the pre-configured {@link FundingMapper}.
   */
  public ProjectMapper(boolean strict, FundingMapper fundingMapper) {
    this.strict = strict;
    this.fundingMapper = fundingMapper;
  }

  /**
   * Convert a DAMAP-specific project to the common standard.
   *
   * @param project a DAMAP project
   * @return a common standard project
   */
  public Project convert(ProjectDO project) {
    var result = new Project();
    convertInto(project, result);
    return result;
  }

  private void convertInto(ProjectDO project, Project result) {
    result.setTitle(project.getTitle());
    result.setDescription(project.getDescription());
    if (project.getStart() != null) {
      result.setStart(LocalDate.ofInstant(project.getStart().toInstant(), ZoneId.systemDefault()));
    }
    if (project.getEnd() != null) {
      result.setEnd(LocalDate.ofInstant(project.getEnd().toInstant(), ZoneId.systemDefault()));
    }
    result.setFunding(List.of(fundingMapper.convert(project.getFunding())));
  }

  public DAMAPProject convertToDAMAPProject(ProjectDO project) {
    var result = new DAMAPProject();
    result.setId(String.valueOf(project.getId()));
    convertInto(project, result);
    return result;
  }

  public ProjectDO convert(DAMAPProject project) {
    return convert(project, project.getId());
  }

  /**
   * Convert a common standard project to the DAMAP-specific one.
   *
   * @param project a common standard project
   * @param projectId the ID of the project
   * @return a DAMAP project
   */
  public ProjectDO convert(Project project, String projectId) {
    var result = new ProjectDO();
    if (project.getProjectId() != null && !project.getProjectId().isEmpty()) {
      result.setUniversityId(project.getProjectId().get(0).getIdentifier());
    } else {
      result.setUniversityId(projectId);
    }
    result.setTitle(project.getTitle());
    result.setDescription(project.getDescription());
    var start = project.getStart();
    if (start != null) {
      result.setStart(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
    var end = project.getEnd();
    if (end != null) {
      result.setEnd(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }
    var fundings = project.getFunding();
    if (fundings != null) {
      if (fundings.size() > 1 && strict) {
        throw new CommonStandardCompatibilityException(
            "more than one funding present for project " + project.getTitle());
      }
      result.setFunding(fundingMapper.convert(fundings.get(0)));
    }
    return result;
  }
}
