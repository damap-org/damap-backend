package org.damap.base.rest.dmp.domain;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.TestProfiles;
import org.damap.base.TestSetup;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@JBossLog
@QuarkusTest
@TestProfile(TestProfiles.DefaultProfile.class)
class ValidatorTest extends TestSetup {
  @Inject DmpService dmpService;

  @Test
  @TestSecurity(authorizationEnabled = false)
  void validatorTriggerTest() {
    // set title with length of 300, limit is 255
    dmpDO.setTitle(
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.");

    Assertions.assertThrows(
        ConstraintViolationException.class,
        () -> {
          dmpService.update(dmpDO);
        });
  }
}
