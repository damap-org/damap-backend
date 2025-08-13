package org.damap.base.integration.mock;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.integration.ProjectServiceProvider;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.ProjectSupplementDO;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * This class is a simplified implementation on how to implement a project list integration.
 *
 * <p>Note: traditionally, per-university custom integrations would extend this class. However, this
 * practice is no longer recommended. As the name suggests, this is a mock, not a fully functional
 * implementation.
 */
@JBossLog
@ApplicationScoped
public class MockProjectServiceImpl implements ProjectServiceProvider {

  @Inject @RestClient MockPersonRestService mockPersonRestService;

  @Inject @RestClient MockProjectRestService mockProjectRestService;

  /** {@inheritDoc} */
  @Override
  public List<ContributorDO> getProjectStaff(String projectId) {
    // This integration does not support a per-project staff, so we are just fetching all staff:
    return mockPersonRestService.getContributorSearchResult();
  }

  /** {@inheritDoc} */
  @Override
  public ProjectSupplementDO getProjectSupplement(String projectId) {
    // This integration does not support fetching project supplement information.
    return null;
  }

  /** {@inheritDoc} */
  @Override
  public ContributorDO getProjectLeader(String projectId) {
    // This integration does not support roles, so we will just return the first person from the
    // person list.
    List<ContributorDO> response = mockPersonRestService.getContributorSearchResult();
    if (response.isEmpty()) {
      return null;
    }
    return response.get(0);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ProjectDO> search(Search search) {
    var items = mockProjectRestService.getProjectList(search.getQuery());

    return ResultList.fromItemsAndSearch(items, search);
  }

  /** {@inheritDoc} */
  @Override
  public ProjectDO read(String id) {
    List<ProjectDO> response = mockProjectRestService.getProjectDetails(id);
    if (response.isEmpty()) {
      return null;
    }
    return response.get(0);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ProjectDO> getRecommended(Search search) {
    var items = mockProjectRestService.getRecommended("recommend");

    return ResultList.fromItemsAndSearch(items, search);
  }
}
