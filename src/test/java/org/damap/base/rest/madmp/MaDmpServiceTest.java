package org.damap.base.rest.madmp;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.rest.dmp.mapper.MapperService;
import org.damap.base.rest.madmp.dto.Dmp;
import org.damap.base.rest.madmp.mapper.MaDmpMapper;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(TestProfiles.DefaultProfile.class)
class MaDmpServiceTest extends TestSetup {

  @Inject TestDOFactory testDOFactory;

  @Inject MapperService mapperService;

  @Test
  @TestSecurity(authorizationEnabled = false)
  void getByIdTest() {
    Dmp dmp =
        MaDmpMapper.mapToMaDmp(testDOFactory.getOrCreateTestDmpDO(), new Dmp(), mapperService);
    Assertions.assertNotNull(dmp);
  }

  @Test
  @TestSecurity(authorizationEnabled = false)
  void getByIdEmptyTest() {
    Dmp dmp =
        MaDmpMapper.mapToMaDmp(testDOFactory.getOrCreateTestDmpDOEmpty(), new Dmp(), mapperService);
    Assertions.assertNotNull(dmp);
  }
}
