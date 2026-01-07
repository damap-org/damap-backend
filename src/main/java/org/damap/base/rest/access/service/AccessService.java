package org.damap.base.rest.access.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Access;
import org.damap.base.domain.Dmp;
import org.damap.base.domain.User;
import org.damap.base.repo.AccessRepo;
import org.damap.base.repo.DamapUserRepo;
import org.damap.base.repo.DmpRepo;
import org.damap.base.rest.access.domain.AccessDO;
import org.damap.base.rest.access.domain.UserDO;
import org.damap.base.rest.access.mapper.AccessMapper;
import org.damap.base.rest.dmp.mapper.MapperService;

/** AccessService class. */
@ApplicationScoped
@JBossLog
public class AccessService {

  @Inject AccessRepo accessRepo;

  @Inject DmpRepo dmpRepo;

  @Inject MapperService mapperService;

  @Inject DamapUserRepo damapUserRepo;

  /**
   * getByDmpId. Returns all users that have access to the DMP.
   *
   * @param dmpId a long
   * @return a {@link java.util.List} object
   */
  public List<AccessDO> getByDmpId(long dmpId) {
    Dmp dmp = dmpRepo.findById(dmpId);
    List<AccessDO> accessDOList = new ArrayList<>();

    accessRepo
        .getAccessByDmp(dmp)
        .forEach(
            access -> {
              User user = damapUserRepo.findUserByIdentifier(access.getUniversityId());
              AccessDO accessDO = AccessMapper.mapEntityToDO(access, user, new AccessDO());
              accessDOList.add(accessDO);
            });

    return accessDOList;
  }

  /**
   * create.
   *
   * @param accessDO a {@link org.damap.base.rest.access.domain.AccessDO} object
   * @return a {@link org.damap.base.rest.access.domain.AccessDO} object
   */
  @Transactional
  public AccessDO create(AccessDO accessDO) {
    // There should ever only be one access, so automatically delete the older ones (editing
    // existing access is not supported)
    if (accessDO.getId() != null) {
      accessRepo.deleteById(accessDO.getId());
    }

    Access access = new Access();
    AccessMapper.mapDOtoEntity(accessDO, access, mapperService);
    if (access.getPersonIdentifier() != null) {
      access.getPersonIdentifier().persist();
    }
    access.setStart(new Date());
    access.persist();

    User user = damapUserRepo.findUserByIdentifier(access.getUniversityId());

    return AccessMapper.mapEntityToDO(access, user, new AccessDO());
  }

  /**
   * delete.
   *
   * @param id a long
   */
  @Transactional
  public void delete(long id) {
    accessRepo.deleteById(id);
  }

  public List<UserDO> searchUserDO(String match) {
    List<UserDO> result = new ArrayList<>();
    damapUserRepo
        .findUsersByNameOrEmailMatch(match)
        .forEach(
            user -> {
              UserDO userDO = new UserDO();
              userDO.setIdentifier(user.getIdentifier());
              userDO.setName(user.getName());
              userDO.setFirstName(user.getFirstName());
              userDO.setLastName(user.getLastName());
              userDO.setEmail(user.getEmail());
              result.add(userDO);
            });
    return result;
  }
}
