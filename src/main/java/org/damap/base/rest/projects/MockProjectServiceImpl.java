package org.damap.base.rest.projects;

import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.persons.MockPersonRestService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/*
   extend this class in your custom project, for your implementation
*/

/** MockProjectServiceImpl class. */
@ApplicationScoped
@DefaultBean
public class MockProjectServiceImpl implements ProjectService {

  @Inject @RestClient MockPersonRestService mockPersonRestService;

  @Inject @RestClient MockProjectRestService mockProjectRestService;

  /** {@inheritDoc} */
  @Override
  public List<ContributorDO> getProjectStaff(String projectId) {
    return mockPersonRestService.getContributorSearchResult();
  }

  /** {@inheritDoc} */
  @Override
  public ProjectSupplementDO getProjectSupplement(String projectId) {
    try {
      return mockProjectRestService.getProjectSupplement();
    } catch (ProcessingException pe) {
      return null;
    }
  }

  /** {@inheritDoc} */
  @Override
  public ContributorDO getProjectLeader(String projectId) {
    List<ContributorDO> response = mockPersonRestService.getContributorSearchResult();
    if (response.isEmpty()) {
      return null;
    }
    return response.get(0);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ProjectDO> search(MultivaluedMap<String, String> queryParams) {
    var search = Search.fromMap(queryParams);
    var items = mockProjectRestService.getProjectList(search.getQuery());

    return ResultList.fromItemsAndSearch(items, search);
  }

  /** {@inheritDoc} */
  @Override
  public ProjectDO read(String id, MultivaluedMap<String, String> queryParams) {
    List<ProjectDO> response = mockProjectRestService.getProjectDetails(id);
    if (response.isEmpty()) {
      return null;
    }
    return response.get(0);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ProjectDO> getRecommended(MultivaluedMap<String, String> queryParams) {
    var search = Search.fromMap(queryParams);
    var items = mockProjectRestService.getRecommended("recommend");

    return ResultList.fromItemsAndSearch(items, search);
  }
}
