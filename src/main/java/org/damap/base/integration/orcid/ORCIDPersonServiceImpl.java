package org.damap.base.integration.orcid;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.integration.PersonService;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** ORCIDPersonServiceImpl class. */
@ApplicationScoped
@Priority(-1)
@JBossLog
public class ORCIDPersonServiceImpl implements PersonService {

  @Inject @RestClient OrcidPersonService orcidRestClient;

  /** {@inheritDoc} */
  @Override
  public ContributorDO read(String id) {
    return ORCIDMapper.mapRecordEntityToPersonDO(orcidRestClient.get(id));
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ContributorDO> search(Search search) {
    List<ContributorDO> contributors = null;
    try {
      var orcidSearch =
          orcidRestClient.getAll(search.getQuery(), search.getPagination().getPerPage());

      if (orcidSearch.getNumFound() > 0 && orcidSearch.getPersons() != null) {
        contributors =
            orcidSearch.getPersons().stream()
                .map(ORCIDMapper::mapExpandedSearchPersonEntityToDO)
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error("Issue searching ORCID persons", e);
    }

    return ResultList.fromItemsAndSearch(contributors, search);
  }
}
