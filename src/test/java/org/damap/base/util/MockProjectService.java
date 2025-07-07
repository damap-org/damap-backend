package org.damap.base.util;

import io.quarkus.test.InjectMock;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.damap.base.integration.mock.MockProjectServiceImpl;
import org.damap.base.rest.dmp.domain.ContributorDO;

@Priority(2)
@ApplicationScoped
public class MockProjectService extends MockProjectServiceImpl {
  @Inject TestDOFactory testDOFactory;

  @InjectMock MockProjectServiceImpl projectService;

  /** {@inheritDoc} */
  @Override
  public ContributorDO getProjectLeader(String projectId) {
    return testDOFactory.getTestContributorDO();
  }
}
