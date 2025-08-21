package org.damap.base.rest.dmp;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.repo.DmpRepo;
import org.damap.base.repo.DmpVersionRepo;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.rest.version.VersionDO;
import org.damap.base.rest.version.VersionService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Test;

@QuarkusTest
@JBossLog
@TestProfile(TestProfiles.DefaultProfile.class)
class VersionTest extends TestSetup {

  @Inject TestDOFactory testDOFactory;

  @Inject VersionService versionService;

  @Inject DmpService dmpService;

  @Test
  void createVersionTest() {
    VersionDO versionDO = testDOFactory.getOrCreateTestVersionDO();
    assertNotNull(versionDO);
    assertNotNull(versionDO.getId());
    assertNotNull(versionDO.getDmpId());
    assertNotNull(versionDO.getVersionDate());
    assertNotNull(versionDO.getRevisionNumber());
    assertEquals("TestVersion", versionDO.getVersionName());
  }

  @Test
  void getVersionListTest() {
    VersionDO versionDO = testDOFactory.getOrCreateTestVersionDO();

    assertEquals("TestVersion", versionDO.getVersionName());

    List<VersionDO> versionDOList = versionService.getDmpVersions(versionDO.getDmpId());
    assertNotNull(versionDOList);
    assertFalse(versionDOList.isEmpty());

    System.out.println("size " + versionDOList.size());

    assertEquals(versionDO.getId(), versionDOList.get(versionDOList.size() - 1).getId());
    assertEquals("TestVersion", versionDOList.get(versionDOList.size() - 1).getVersionName());
  }

  @Test
  void updateVersionTest() {
    VersionDO versionDO = testDOFactory.getOrCreateTestVersionDO();
    versionDO.setVersionName("TestVersionUpdate");
    var versionDOupdate = versionService.createOrUpdate(versionDO);

    assertNotNull(versionDOupdate);
    assertEquals(versionDO.getId(), versionDOupdate.getId());
    assertEquals(versionDO.getRevisionNumber(), versionDOupdate.getRevisionNumber());
    assertNotEquals("TestVersion", versionDOupdate.getVersionName());
  }

  @Test
  void updateDMPTest() {
    VersionDO versionDO = testDOFactory.getOrCreateTestVersionDO();

    DmpDO dmp = dmpService.getDmpById(versionDO.getDmpId());
    dmp.setTitle("TestDmp updated for revision");
    dmpService.update(dmp);

    DmpDO dmpDOCurrent = dmpService.getDmpById(versionDO.getDmpId());
    DmpDO dmpDOPreviousVersion =
        dmpService.getDmpByIdAndRevision(versionDO.getDmpId(), versionDO.getRevisionNumber());

    assertNotEquals(dmpDOCurrent.getTitle(), dmpDOPreviousVersion.getTitle());
  }
}
