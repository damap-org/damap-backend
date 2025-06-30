package org.damap.base;

import static org.mockito.ArgumentMatchers.any;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.damap.base.integration.mock.MockUniversityPersonServiceImpl;
import org.damap.base.integration.orcid.ORCIDMapper;
import org.damap.base.integration.orcid.ORCIDPersonServiceImpl;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.security.SecurityService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

// Common config for test case setup
/** TestSetup class. */
@QuarkusTest
public class TestSetup {
  @Inject TestDOFactory testDOFactory;

  @InjectMock protected SecurityService securityService;

  @InjectMock MockUniversityPersonServiceImpl personService;

  @InjectMock protected ORCIDPersonServiceImpl orcidPersonServiceImpl;

  protected DmpDO dmpDO;

  /** setup. */
  @BeforeEach
  public void setup() {
    Mockito.when(securityService.getUserId()).thenReturn("012345");
    Mockito.when(securityService.getUserName()).thenReturn("testUser");
    Mockito.when(personService.read(any(String.class)))
        .thenReturn(testDOFactory.getTestContributorDO());
    Mockito.when(orcidPersonServiceImpl.read(any(String.class)))
        .thenReturn(testDOFactory.getTestContributorDO());
    Mockito.when(orcidPersonServiceImpl.read(any(String.class)))
        .thenReturn(ORCIDMapper.mapRecordEntityToPersonDO(testDOFactory.getORCIDTestRecord()));
    dmpDO = testDOFactory.getOrCreateTestDmpDO();
  }
}
