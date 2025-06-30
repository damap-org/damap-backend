package org.damap.base.integration.mock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.integration.PersonService;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/*
   extend this class in your custom project, for your implementation
*/

/**
 * A demonstration how a {@link PersonService} could be integrated.
 *
 * <p>Note: traditionally, per-university custom integrations would extend this class. However, this
 * practice is no longer recommended. As the name suggests, this is a mock, not a fully functional
 * implementation.
 */
@JBossLog
@ApplicationScoped
public class MockUniversityPersonServiceImpl implements PersonService {

  @Inject @RestClient MockPersonRestService mockPersonRestService;

  /** {@inheritDoc} */
  @Override
  public ContributorDO read(String id) {
    List<ContributorDO> response = mockPersonRestService.getContributorDetails(id);
    if (response.isEmpty()) {
      return null;
    }
    return response.get(0);
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ContributorDO> search(Search search) {
    var items = mockPersonRestService.getContributorSearchResult();
    return ResultList.fromItemsAndSearch(items, search);
  }
}
