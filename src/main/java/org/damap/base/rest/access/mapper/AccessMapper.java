package org.damap.base.rest.access.mapper;

import lombok.experimental.UtilityClass;
import org.damap.base.domain.Access;
import org.damap.base.domain.Dmp;
import org.damap.base.domain.User;
import org.damap.base.rest.access.domain.AccessDO;
import org.damap.base.rest.dmp.mapper.MapperService;

/** AccessMapper class. */
@UtilityClass
public class AccessMapper {

  /**
   * mapEntityToDO.
   *
   * @param access a {@link org.damap.base.domain.Access} object
   * @param accessDO a {@link org.damap.base.rest.access.domain.AccessDO} object
   * @return a {@link org.damap.base.rest.access.domain.AccessDO} object
   */
  public AccessDO mapEntityToDO(Access access, User user, AccessDO accessDO) {
    accessDO.setId(access.id);
    accessDO.setDmpId(access.getDmp().id);
    accessDO.setIdentifier(access.getUniversityId());

    if (user != null) {
      accessDO.setMbox(user.getEmail());
      accessDO.setFirstName(user.getFirstName());
      accessDO.setLastName(user.getLastName());
    }

    accessDO.setRole(access.getRole());
    accessDO.setStart(access.getStart());
    accessDO.setUntil(access.getUntil());
    return accessDO;
  }

  /**
   * mapDOtoEntity.
   *
   * @param accessDO a {@link org.damap.base.rest.access.domain.AccessDO} object
   * @param access a {@link org.damap.base.domain.Access} object
   * @param mapperService a {@link org.damap.base.rest.dmp.mapper.MapperService} object
   * @return a {@link org.damap.base.domain.Access} object
   */
  public Access mapDOtoEntity(AccessDO accessDO, Access access, MapperService mapperService) {
    Dmp dmp = mapperService.getDmpById(accessDO.getDmpId());
    access.setDmp(dmp);
    access.setRole(accessDO.getRole());
    access.setUniversityId(accessDO.getIdentifier());

    access.setStart(accessDO.getStart());
    access.setUntil(accessDO.getUntil());
    return access;
  }
}
