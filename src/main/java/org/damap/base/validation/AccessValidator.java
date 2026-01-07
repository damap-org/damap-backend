package org.damap.base.validation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;
import org.damap.base.domain.Access;
import org.damap.base.domain.Dmp;
import org.damap.base.enums.EFunctionRole;
import org.damap.base.repo.AccessRepo;
import org.damap.base.repo.DamapUserRepo;
import org.damap.base.repo.DmpRepo;
import org.damap.base.rest.access.domain.AccessDO;
import org.damap.base.security.SecurityService;

/** AccessValidator class. */
@ApplicationScoped
public class AccessValidator {

  @Inject AccessRepo accessRepo;

  @Inject DmpRepo dmpRepo;

  @Inject SecurityService securityService;

  @Inject DamapUserRepo userRepo;

  /**
   * canViewDmp.
   *
   * @param dmpId a long
   * @param personId a {@link java.lang.String} object
   * @return a boolean
   */
  public boolean canViewDmp(long dmpId, String personId) {
    if (securityService.isAdmin()) {
      return true;
    }

    List<Access> accessList = accessRepo.getAllByUniversityId(personId);

    Optional<Access> dmpAccess =
        accessList.stream().filter(access -> access.getDmp().id.equals(dmpId)).findAny();

    return dmpAccess.isPresent();
  }

  /**
   * canEditDmp.
   *
   * @param dmpId a long
   * @param personId a {@link java.lang.String} object
   * @return a boolean
   */
  public boolean canEditDmp(long dmpId, String personId) {
    if (securityService.isAdmin()) {
      return true;
    }

    List<Access> accessList = accessRepo.getAllByUniversityId(personId);

    Optional<Access> dmpAccess =
        accessList.stream()
            .filter(
                access ->
                    access.getDmp().id.equals(dmpId)
                        && (access.getRole().equals(EFunctionRole.EDITOR)
                            || access.getRole().equals(EFunctionRole.OWNER)))
            .findAny();

    return dmpAccess.isPresent();
  }

  /**
   * canExportDmp.
   *
   * @param dmpId a long
   * @param personId a {@link java.lang.String} object
   * @return a boolean
   */
  public boolean canExportDmp(long dmpId, String personId) {
    return this.canViewDmp(dmpId, personId);
  }

  /**
   * canDeleteDmp.
   *
   * @param dmpId a long
   * @param personId a {@link java.lang.String} object
   * @return a boolean
   */
  public boolean canDeleteDmp(long dmpId, String personId) {
    if (securityService.isAdmin()) {
      return true;
    }

    List<Access> accessList = accessRepo.getAllByUniversityId(personId);

    Optional<Access> dmpAccess =
        accessList.stream()
            .filter(
                access ->
                    access.getDmp().id.equals(dmpId)
                        && access.getRole().equals(EFunctionRole.OWNER))
            .findAny();

    return dmpAccess.isPresent();
  }

  /**
   * canViewAccess.
   *
   * @param dmpId a long
   * @return a boolean
   */
  public boolean canViewAccess(long dmpId) {
    if (securityService.isAdmin()) {
      return true;
    }

    Dmp dmp = dmpRepo.findById(dmpId);
    if (dmp == null) {
      return false;
    }

    List<Access> accessList = accessRepo.getAccessByDmp(dmp);
    Optional<Access> dmpAccess =
        accessList.stream()
            .filter(
                access ->
                    access.getUniversityId().equals(securityService.getUserId())
                        && (access.getRole().equals(EFunctionRole.OWNER)))
            .findAny();

    return dmpAccess.isPresent();
  }

  /**
   * canCreateAccess.
   *
   * @param accessDO a {@link org.damap.base.rest.access.domain.AccessDO} object
   * @return a boolean
   */
  public boolean canCreateAccess(AccessDO accessDO) {

    Dmp dmp = dmpRepo.findById(accessDO.getDmpId());
    if (dmp == null) {
      return false;
    }

    // Check user permission to create new access
    boolean hasPermission = securityService.isAdmin();
    if (!hasPermission) {
      List<Access> accessList = accessRepo.getAccessByDmp(dmp);
      // Check if caller is owner
      Optional<Access> callerOwnerAccess =
          accessList.stream()
              .filter(
                  access ->
                      access.getUniversityId().equals(securityService.getUserId())
                          && access.getRole().equals(EFunctionRole.OWNER))
              .findAny();
      hasPermission = callerOwnerAccess.isPresent();
    }

    return canGetAccess(accessDO) && hasPermission;
  }

  /**
   * canDeleteAccess.
   *
   * @param id a long
   * @return a boolean
   */
  public boolean canDeleteAccess(long id) {
    Access accessToDelete = accessRepo.findById(id);

    // Check if user has permission to delete access
    if (securityService.isAdmin()) {
      return true;
    }

    // Non-existing access can be deleted (idempotence)
    if (accessToDelete == null) {
      return true;
    }

    List<Access> accessForDmp = accessRepo.getAccessByDmp(accessToDelete.getDmp());

    List<EFunctionRole> callerAccessRoles =
        accessForDmp.stream()
            .filter(a -> a.getUniversityId().equals(securityService.getUserId()))
            .map(Access::getRole)
            .toList();

    // Only owners can delete access
    if (!callerAccessRoles.contains(EFunctionRole.OWNER)) {
      return false;
    }

    // There has to be one owner always
    if (accessToDelete.getRole().equals(EFunctionRole.OWNER)) {
      return accessForDmp.stream().filter(a -> a.getRole() == EFunctionRole.OWNER).limit(2).count()
          >= 2;
    }

    return true;
  }

  /**
   * canGetAccess. Checks if the user we are trying to add actually exists in our system.
   *
   * @param accessDO a {@link org.damap.base.rest.access.domain.AccessDO} object
   * @return a boolean
   */
  public boolean canGetAccess(AccessDO accessDO) {
    return userRepo.findUserByIdentifier(accessDO.getIdentifier()) != null;
  }
}
