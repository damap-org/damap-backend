package org.damap.base.rest.dmp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.Access;
import org.damap.base.domain.Contributor;
import org.damap.base.domain.Dmp;
import org.damap.base.enums.EContributorRole;
import org.damap.base.enums.EFunctionRole;
import org.damap.base.enums.EIdentifierType;
import org.damap.base.repo.AccessRepo;
import org.damap.base.repo.DmpRepo;
import org.damap.base.rest.dmp.domain.*;
import org.damap.base.rest.dmp.mapper.ContributorDOMapper;
import org.damap.base.rest.dmp.mapper.DmpDOMapper;
import org.damap.base.rest.dmp.mapper.DmpListItemDOMapper;
import org.damap.base.rest.dmp.mapper.MapperService;
import org.damap.base.rest.dmp.mapper.ProjectSupplementDOMapper;
import org.damap.base.rest.persons.orcid.ORCIDPersonServiceImpl;
import org.damap.base.rest.projects.ProjectService;
import org.damap.base.rest.projects.ProjectSupplementDO;
import org.damap.base.rest.storage.InternalStorageService;
import org.damap.base.rest.version.VersionDO;
import org.damap.base.rest.version.VersionService;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

/** DmpService class. */
@ApplicationScoped
@JBossLog
public class DmpService {

  @Inject DmpRepo dmpRepo;

  @Inject AccessRepo accessRepo;

  @Inject ProjectService projectService;

  @Inject MapperService mapperService;

  @Inject VersionService versionService;

  @Inject ORCIDPersonServiceImpl orcidPersonService;

  @Inject InternalStorageService internalStorageService;

  /**
   * getAll.
   *
   * @return a {@link java.util.List} object
   */
  public List<DmpListItemDO> getAll() {

    List<Dmp> dmpList = dmpRepo.getAll();
    List<DmpListItemDO> dmpListItemDOList = new ArrayList<>();
    dmpList.forEach(
        dmp ->
            dmpListItemDOList.add(
                DmpListItemDOMapper.mapEntityToDO(
                    null, dmp, new DmpListItemDO(), versionService.getDmpVersions(dmp.id))));
    return dmpListItemDOList;
  }

  /**
   * getDmpListByPersonId.
   *
   * @param personId a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @Transactional
  public List<DmpListItemDO> getDmpListByPersonId(String personId) {

    List<Access> accessList = accessRepo.getAllDmpByUniversityId(personId);

    List<DmpListItemDO> dmpListItemDOS = new ArrayList<>();
    accessList.forEach(
        access ->
            dmpListItemDOS.add(
                DmpListItemDOMapper.mapEntityToDO(
                    access,
                    access.getDmp(),
                    new DmpListItemDO(),
                    versionService.getDmpVersions(access.getDmp().id))));
    return dmpListItemDOS;
  }

  /**
   * getDmpById.
   *
   * @param dmpId a long
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @Transactional
  public DmpDO getDmpById(long dmpId) {
    return DmpDOMapper.mapEntityToDO(dmpRepo.findById(dmpId), new DmpDO());
  }

  /**
   * getDmpDOListByPersonId.
   *
   * @param personId a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @Transactional
  public List<DmpDO> getDmpDOListByPersonId(String personId) {
    List<Access> accessList = accessRepo.getAllDmpByUniversityId(personId);

    List<DmpDO> dmpDOS = new ArrayList<>();
    accessList.forEach(
        access -> dmpDOS.add(DmpDOMapper.mapEntityToDO(access.getDmp(), new DmpDO())));
    return dmpDOS;
  }

  /**
   * create.
   *
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @param editedBy a {@link java.lang.String} object
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @Transactional
  public DmpDO create(@Valid DmpDO dmpDO, String editedBy) {
    log.info("Creating new DMP");
    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);

    DmpConsistencyUtility.enforceActiveStorage(dmpDO, null, internalStorageService);

    Dmp dmp = DmpDOMapper.mapDOtoEntity(dmpDO, new Dmp(), mapperService);
    dmp.setCreated(new Date());
    fetchORCIDContributorInfo(dmp);
    updateDmpSupplementalInfo(dmp);
    updateProjectLead(dmp);
    dmp.persistAndFlush();
    createAccess(dmp, editedBy);
    return getDmpById(dmp.id);
  }

  /**
   * update.
   *
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @Transactional
  public DmpDO update(@Valid DmpDO dmpDO) {
    log.info("Updating DMP with id " + dmpDO.getId());
    DmpConsistencyUtility.enforceDmpConsistency(dmpDO);
    Dmp dmp = dmpRepo.findById(dmpDO.getId());

    DmpConsistencyUtility.enforceActiveStorage(dmpDO, dmp, internalStorageService);

    boolean projectSelectionChanged = projectSelectionChanged(dmp, dmpDO);
    DmpDOMapper.mapDOtoEntity(dmpDO, dmp, mapperService);
    dmp.setModified(new Date());
    fetchORCIDContributorInfo(dmp);
    if (projectSelectionChanged) {
      updateDmpSupplementalInfo(dmp);
      updateProjectLead(dmp);
    }
    dmp.persistAndFlush();
    return getDmpById(dmp.id);
  }

  /**
   * delete.
   *
   * @param dmpId a long
   */
  @Transactional
  public void delete(long dmpId) {
    log.info("Deleting DMP with id " + dmpId);
    Dmp dmp = dmpRepo.findById(dmpId);
    this.removeAccess(dmp);
    this.removeVersions(dmp);
    dmpRepo.deleteById(dmpId);
  }

  /**
   * createAccess.
   *
   * @param dmp a {@link org.damap.base.domain.Dmp} object
   * @param editedById a {@link java.lang.String} object
   */
  public void createAccess(Dmp dmp, String editedById) {
    Access access = new Access();
    access.setUniversityId(editedById);
    access.setRole(EFunctionRole.OWNER);
    access.setDmp(dmp);
    access.setStart(new Date());
    access.persistAndFlush();
  }

  private void removeAccess(Dmp dmp) {
    List<Access> access = accessRepo.getAccessByDmp(dmp);
    access.forEach(Access::delete);
  }

  private void removeVersions(Dmp dmp) {
    List<VersionDO> versionDOs = versionService.getDmpVersions(dmp.id);
    for (VersionDO versionDO : versionDOs) {
      versionService.delete(versionDO.getId());
    }
  }

  /**
   * getDefaultFileName.
   *
   * @param id a long
   * @return a {@link java.lang.String} object
   */
  public String getDefaultFileName(long id) {
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");

    String filename = "My Data Management Plan";

    Dmp dmp = dmpRepo.findById(id);
    if (dmp != null) {
      if (dmp.getProject() != null) {
        if (dmp.getProject().getUniversityId() != null
            && projectService.read(dmp.getProject().getUniversityId()) != null) {
          filename =
              "DMP_"
                  + projectService.read(dmp.getProject().getUniversityId()).getAcronym()
                  + "_"
                  + formatter.format(date);
        } else if (dmp.getProject().getTitle() != null)
          filename = "DMP_" + dmp.getProject().getTitle() + "_" + formatter.format(date);
      } else {
        if (dmp.getTitle() != null) filename = dmp.getTitle();
      }
    }

    filename = filename.replaceAll("[\"',\\s]+", "_");

    return filename;
  }

  /**
   * checkExistingDmps.
   *
   * @param projectDOList a {@link java.util.List} object
   * @return a {@link java.util.List} object
   */
  public List<ProjectDO> checkExistingDmps(List<ProjectDO> projectDOList) {

    for (Dmp dmp : dmpRepo.getAll()) {
      for (ProjectDO projectDO : projectDOList) {
        if (dmp.getProject() != null
            && dmp.getProject().getUniversityId() != null
            && projectDO.getUniversityId() != null
            && dmp.getProject().getUniversityId().equals(projectDO.getUniversityId()))
          projectDO.setDmpExists(true);
      }
    }
    return projectDOList;
  }

  // This method will retrieve the Project Supplement values from the connected
  // CRIS System and it will reset them to null in case the project is not from a
  // connected system.
  private void updateDmpSupplementalInfo(Dmp dmp) {
    if (dmp.getProject() != null) {
      ProjectSupplementDO projectSupplementDO = null;
      if (dmp.getProject().getUniversityId() != null) {
        projectSupplementDO =
            projectService.getProjectSupplement(dmp.getProject().getUniversityId());
      }
      if (projectSupplementDO == null) projectSupplementDO = new ProjectSupplementDO();
      ProjectSupplementDOMapper.mapDOtoEntity(projectSupplementDO, dmp);
    }
  }

  /**
   * Retrieve the project leader of the DMPs project. Set the project leader as contact, if there is
   * no other contact selected. Add the project leader as a contributor, if it is not already added.
   *
   * @param dmp Data management plan
   */
  private void updateProjectLead(Dmp dmp) {
    if (dmp.getProject() == null || dmp.getProject().getUniversityId() == null) return;

    ContributorDO projectLeaderDO =
        projectService.getProjectLeader(dmp.getProject().getUniversityId());
    if (projectLeaderDO == null) return;

    List<Contributor> dmpContributors = dmp.getContributorList();

    Optional<Contributor> alreadyExistingContributorLeader =
        dmpContributors.stream()
            .filter(c -> c.getUniversityId().equals(projectLeaderDO.getUniversityId()))
            .findFirst();

    Contributor projectLeaderContributor =
        alreadyExistingContributorLeader.orElse(new Contributor());

    // Adding project leader as contributor if it was not there yet.
    if (alreadyExistingContributorLeader.isEmpty()) {
      ContributorDOMapper.mapDOtoEntity(projectLeaderDO, projectLeaderContributor);
      projectLeaderContributor.setDmp(dmp);
      dmpContributors.add(projectLeaderContributor);
    }

    // If no role was defined, set contributor role to project leader
    if (projectLeaderContributor.getContributorRole() == null) {
      projectLeaderContributor.setContributorRole(EContributorRole.PROJECT_LEADER);
    }

    // If no other contact was defined, set project leader as contact.
    if (dmpContributors.stream().noneMatch(c -> c.getContact())) {
      projectLeaderContributor.setContact(true);
    }
  }

  private void fetchORCIDContributorInfo(Dmp dmp) {
    dmp.getContributorList()
        .forEach(
            contributor -> {
              // Existing contributor? Do not fetch data.
              if (contributor.id != null) {
                return;
              }

              var identifier = contributor.getPersonIdentifier();
              if (identifier != null
                  && identifier.getIdentifierType().equals(EIdentifierType.ORCID)) {
                try {
                  ContributorDO contributorDO = orcidPersonService.read(identifier.getIdentifier());
                  if (contributor.getMbox() == null || contributor.getMbox().isEmpty())
                    contributor.setMbox(contributorDO.getMbox());

                  if (contributor.getAffiliation() == null
                      || contributor.getAffiliation().isEmpty())
                    contributor.setAffiliation(contributorDO.getAffiliation());

                  if (contributor.getFirstName() == null || contributor.getFirstName().isEmpty())
                    contributor.setFirstName(contributorDO.getFirstName());

                  if (contributor.getLastName() == null || contributor.getLastName().isEmpty())
                    contributor.setLastName(contributorDO.getLastName());

                } catch (Exception e) {
                  log.warn(
                      String.format(
                          "Could not fetch ORCID or map contributor info for identifier '%s'.%nDetail error message: %s",
                          identifier.getIdentifier(), e));
                }
              }
            });
  }

  private boolean projectSelectionChanged(Dmp dmp, DmpDO dmpDO) {

    if (dmpDO.getProject() == null) return false;
    if (dmp.getProject() == null) return true;

    return !Objects.equals(dmp.getProject().id, dmpDO.getProject().getId())
        || !Objects.equals(
            dmp.getProject().getUniversityId(), dmpDO.getProject().getUniversityId());
  }

  /**
   * getDmpByIdAndRevision.
   *
   * @param dmpId a long
   * @param revision a long
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  public DmpDO getDmpByIdAndRevision(long dmpId, long revision) {
    AuditReader reader = AuditReaderFactory.get(dmpRepo.getEntityManager());
    Dmp dmpRevision = reader.find(Dmp.class, dmpId, revision);
    return DmpDOMapper.mapEntityToDO(dmpRevision, new DmpDO());
  }
}
