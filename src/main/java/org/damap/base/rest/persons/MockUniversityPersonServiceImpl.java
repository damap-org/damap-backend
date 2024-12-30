package org.damap.base.rest.persons;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/*
   extend this class in your custom project, for your implementation
*/

/** MockUniversityPersonServiceImpl class. */
@ApplicationScoped
public class MockUniversityPersonServiceImpl implements PersonService {

  @Inject @RestClient MockPersonRestService mockPersonRestService;

  /** {@inheritDoc} */
  @Override
  public ContributorDO read(String id, MultivaluedMap<String, String> queryParams) {
    List<ContributorDO> response = mockPersonRestService.getContributorDetails(id);
    if (response.isEmpty()) {
      return null;
    }
    return response.get(0);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ContributorDO> search(MultivaluedMap<String, String> queryParams) {
    var items = mockPersonRestService.getContributorSearchResult();
    var search = Search.fromMap(queryParams);

    return ResultList.fromItemsAndSearch(items, search);
  }
}
