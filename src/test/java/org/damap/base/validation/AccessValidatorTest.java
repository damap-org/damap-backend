package org.damap.base.validation;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.List;
import org.damap.base.TestProfiles;
import org.damap.base.domain.Access;
import org.damap.base.domain.Dmp;
import org.damap.base.enums.EFunctionRole;
import org.damap.base.repo.AccessRepo;
import org.damap.base.security.SecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
@TestProfile(TestProfiles.DefaultProfile.class)
class AccessValidatorTest {

  @Inject AccessValidator accessValidator;

  @InjectMock AccessRepo accessRepo;

  @InjectMock SecurityService securityService;

  String ownerId = "-1";
  String editorId = "-2";
  String guestId = "-3";
  String adminId = "-4";

  /** setup. */
  @BeforeEach
  public void setup() {
    Mockito.when(securityService.isAdmin()).thenReturn(false);
    Mockito.when(accessRepo.getAllByUniversityId(ownerId)).thenReturn(getAccessListOwner());
    Mockito.when(accessRepo.getAllByUniversityId(editorId)).thenReturn(getAccessListEditor());
    Mockito.when(accessRepo.getAllByUniversityId(guestId)).thenReturn(getAccessListGuest());
  }

  @Test
  void testCanViewDmp() {
    Assertions.assertTrue(accessValidator.canViewDmp(-1, ownerId));
    Assertions.assertTrue(accessValidator.canViewDmp(-1, editorId));
    Assertions.assertTrue(accessValidator.canViewDmp(-1, guestId));
    Assertions.assertFalse(accessValidator.canViewDmp(-1, adminId));

    Assertions.assertFalse(accessValidator.canViewDmp(-2, ownerId));
    Assertions.assertFalse(accessValidator.canViewDmp(-2, editorId));
    Assertions.assertFalse(accessValidator.canViewDmp(-2, guestId));

    Mockito.when(securityService.isAdmin()).thenReturn(true);
    Assertions.assertTrue(accessValidator.canViewDmp(-1, adminId));
  }

  @Test
  void testCanEditDmp() {
    Assertions.assertTrue(accessValidator.canEditDmp(-1, ownerId));
    Assertions.assertTrue(accessValidator.canEditDmp(-1, editorId));
    Assertions.assertFalse(accessValidator.canEditDmp(-1, guestId));
    Assertions.assertFalse(accessValidator.canEditDmp(-1, adminId));

    Assertions.assertFalse(accessValidator.canEditDmp(-2, ownerId));
    Assertions.assertFalse(accessValidator.canEditDmp(-2, editorId));
    Assertions.assertFalse(accessValidator.canEditDmp(-2, guestId));

    Mockito.when(securityService.isAdmin()).thenReturn(true);
    Assertions.assertTrue(accessValidator.canEditDmp(-1, adminId));
  }

  private List<Access> getAccessListOwner() {
    Dmp dmp = new Dmp();
    dmp.id = -1L;
    Access access = new Access();
    access.setUniversityId(ownerId);
    access.setDmp(dmp);
    access.setRole(EFunctionRole.OWNER);

    return List.of(access);
  }

  private List<Access> getAccessListEditor() {
    Dmp dmp = new Dmp();
    dmp.id = -1L;
    Access access = new Access();
    access.setUniversityId(editorId);
    access.setDmp(dmp);
    access.setRole(EFunctionRole.EDITOR);

    return List.of(access);
  }

  private List<Access> getAccessListGuest() {
    Dmp dmp = new Dmp();
    dmp.id = -1L;
    Access access = new Access();
    access.setUniversityId(guestId);
    access.setDmp(dmp);
    access.setRole(EFunctionRole.GUEST);

    return List.of(access);
  }
}
