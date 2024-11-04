package org.damap.base.rest.dmp.mapper;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.damap.base.domain.Access;
import org.damap.base.domain.Dmp;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.DmpListItemDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.version.VersionDO;

/** DmpListItemDOMapper class. */
@UtilityClass
public class DmpListItemDOMapper {

  /**
   * mapEntityToDO.
   *
   * @param access a {@link org.damap.base.domain.Access} object
   * @param dmp a {@link org.damap.base.domain.Dmp} object
   * @param dmpListItemDO a {@link org.damap.base.rest.dmp.domain.DmpListItemDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.DmpListItemDO} object
   */
  public DmpListItemDO mapEntityToDO(
      Access access, Dmp dmp, DmpListItemDO dmpListItemDO, List<VersionDO> versionDOList) {
    dmpListItemDO.setId(dmp.id);
    dmpListItemDO.setTitle(dmp.getTitle());
    dmpListItemDO.setCreated(dmp.getCreated());
    dmpListItemDO.setModified(dmp.getModified());
    dmpListItemDO.setDescription(dmp.getDescription());
    if (access != null) {
      dmpListItemDO.setAccessType(access.getRole());
    }

    if (dmp.getContact() != null) {
      ContributorDO contactDO = new ContributorDO();
      ContributorDOMapper.mapEntityToDO(dmp.getContact(), contactDO);
      dmpListItemDO.setContact(contactDO);
    }

    if (dmp.getProject() != null) {
      ProjectDO projectDO = new ProjectDO();
      ProjectDOMapper.mapEntityToDO(dmp.getProject(), projectDO);
      dmpListItemDO.setProject(projectDO);
    }

    // Get the latest version from the versionDOList with the highest revision number
    if (versionDOList != null && !versionDOList.isEmpty()) {
      dmpListItemDO.setVersionCount((long) versionDOList.size());
      dmpListItemDO.setLatestVersionName(
          versionDOList.get(versionDOList.size() - 1).getVersionName());
    }

    return dmpListItemDO;
  }
}
