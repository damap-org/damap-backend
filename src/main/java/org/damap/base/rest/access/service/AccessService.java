package org.damap.base.rest.access.service;

import static org.damap.base.utils.EqualityUtils.nullExclusiveEquals;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Access;
import org.damap.base.domain.Dmp;
import org.damap.base.repo.AccessRepo;
import org.damap.base.repo.DmpRepo;
import org.damap.base.rest.PersonServiceBroker;
import org.damap.base.rest.access.domain.AccessDO;
import org.damap.base.rest.access.mapper.AccessMapper;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.mapper.ContributorDOMapper;
import org.damap.base.rest.dmp.mapper.MapperService;
import org.damap.base.rest.persons.PersonService;

/** AccessService class. */
@ApplicationScoped
@JBossLog
public class AccessService {

  // defines which personService is to be used for access management
  private final String ENABLED_PERSON_SERVICE = "UNIVERSITY";

  @Inject AccessRepo accessRepo;

  @Inject DmpRepo dmpRepo;

  @Inject MapperService mapperService;

  @Inject PersonServiceBroker personServiceBroker;

  /**
   * getByDmpId.
   *
   * @param dmpId a long
   * @return a {@link java.util.List} object
   */
  public List<ContributorDO> getByDmpId(long dmpId) {
    PersonService personService =
        personServiceBroker.getServiceForQueryParam(ENABLED_PERSON_SERVICE);

    Dmp dmp = dmpRepo.findById(dmpId);
    // Get access list (owner, editors)
    List<ContributorDO> accessDOList = new ArrayList<>();
    accessRepo
        .getAccessByDmp(dmp)
        .forEach(
            access -> {
              AccessDO accessDO = AccessMapper.mapEntityToDO(access, new AccessDO());
              // Set owner/editor data. If they're not listed as a contributor, fields will be
              // empty.
              if (accessDO.getMbox() == null) {
                // TODO: Handle case when owner is no longer at university
                ContributorDO owner = personService.read(access.getUniversityId());
                accessDO.setFirstName(owner.getFirstName());
                accessDO.setLastName(owner.getLastName());
                accessDO.setMbox(owner.getMbox());
              }
              accessDOList.add(accessDO);
            });

    // Get dmp contributors (viewers)
    dmp.getContributorList()
        .forEach(
            contributor -> {
              // Only university members can be editors for now
              if (contributor.getUniversityId() != null
                  && !contributor.getUniversityId().isEmpty()
                  && accessDOList.stream()
                      .noneMatch(
                          a ->
                              nullExclusiveEquals(
                                  a.getUniversityId(), contributor.getUniversityId()))) {
                ContributorDO contributorDO = new ContributorDO();
                ContributorDOMapper.mapEntityToDO(contributor, contributorDO);
                // Set id null, so it's not confused with access id
                contributorDO.setId(null);
                accessDOList.add(contributorDO);
              }
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
    Access access = new Access();
    AccessMapper.mapDOtoEntity(accessDO, access, mapperService);
    if (access.getPersonIdentifier() != null) {
      access.getPersonIdentifier().persist();
    }
    access.setStart(new Date());
    access.persist();
    return AccessMapper.mapEntityToDO(access, new AccessDO());
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
}
