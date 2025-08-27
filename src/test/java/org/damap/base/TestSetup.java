package org.damap.base;

import static org.mockito.ArgumentMatchers.any;

import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import org.damap.base.domain.User;
import org.damap.base.integration.mock.MockProjectServiceImpl;
import org.damap.base.integration.mock.MockUniversityPersonServiceImpl;
import org.damap.base.integration.orcid.ORCIDMapper;
import org.damap.base.integration.orcid.ORCIDPersonServiceImpl;
import org.damap.base.repo.UserRepo;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.security.SecurityService;
import org.damap.base.util.TestDOFactory;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

// Common config for test case setup
/** TestSetup class. */
public class TestSetup {
  @Inject TestDOFactory testDOFactory;

  @InjectMock protected SecurityService securityService;

  @InjectMock MockUniversityPersonServiceImpl personService;

  @InjectMock protected ORCIDPersonServiceImpl orcidPersonServiceImpl;

  @InjectMock MockProjectServiceImpl projectService;

  @InjectMock protected UserRepo userRepo;

  protected DmpDO dmpDO;

  /**
   * @BeforeEach in an extending class overrides this, use super.setup() to circumvent
   */
  @BeforeEach
  public void setup() {
    Mockito.when(securityService.getUserId()).thenReturn("012345");
    Mockito.when(securityService.getUserName()).thenReturn("testUser");
    Mockito.when(personService.read(any(String.class)))
        .thenReturn(testDOFactory.getTestContributorDO());
    Mockito.when(orcidPersonServiceImpl.read(any(String.class)))
        .thenReturn(ORCIDMapper.mapRecordEntityToPersonDO(testDOFactory.getORCIDTestRecord()));
    Mockito.when(projectService.getProjectLeader(any()))
        .thenReturn(testDOFactory.getTestContributorDO());

    Mockito.when(userRepo.findUserBySubject(any(String.class))).thenReturn(User.builder().build());
    dmpDO = testDOFactory.getOrCreateTestDmpDO();
  }
}
