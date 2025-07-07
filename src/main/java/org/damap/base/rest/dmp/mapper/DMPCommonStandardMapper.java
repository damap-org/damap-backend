package org.damap.base.rest.dmp.mapper;

import org.damap.base.integration.rest.*;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.FundingDO;
import org.damap.base.rest.dmp.domain.ProjectDO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class DMPCommonStandardMapper {
  public static DMPWithID convert(DmpDO dmp) {
    return new DMPWithID().id(String.valueOf(dmp.getId())).dmp(convertData(dmp));
  }

  private static DMPData convertData(DmpDO dmp) {
    DMPData result = new DMPData();
    ProjectDO project = dmp.getProject();
    if (project != null) {
      result.dmpId(
        new DMPID().identifier(
          dmp.getProject().getUniversityId()
        ).type(DMPIDType.OTHER)
      );

      result.project(convertProject(project));
    }
    return result;
  }

  private static List<Project> convertProject(ProjectDO project) {
    var result = new Project();
    result.setTitle(project.getTitle());
    result.setDescription(project.getDescription());
    if (project.getStart() != null) {
      result.setStart(LocalDate.ofInstant(project.getStart().toInstant(), ZoneId.of("UTC")));
    }
    if (project.getEnd() != null) {
      result.setEnd(LocalDate.ofInstant(project.getEnd().toInstant(), ZoneId.of("UTC")));
    }
    result.setFunding(convertFunding(project.getFunding()));

  }

  private static List<Funding> convertFunding(FundingDO funding) {
    return null;
  }
}
