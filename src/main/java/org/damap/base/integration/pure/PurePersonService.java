package org.damap.base.integration.pure;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.integration.PersonService;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;

/**
 * This service implements partially reading Elsevier Pure Person objects from their API.
 *
 * <p><strong>Note:</strong> this implementation is currently experimental.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
@ApplicationScoped
@JBossLog
public class PurePersonService implements PersonService {
  @Inject PureAPI pureAPI;

  /** {@inheritDoc} */
  @Override
  public ContributorDO read(String id) {
    PureAPIPerson person = pureAPI.getPerson(id);
    if (person == null) {
      return null;
    }
    return person.toContributor();
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ContributorDO> search(Search search) {
    ResultList<ContributorDO> result = new ResultList<>();
    result.setSearch(search);
    Stream<ContributorDO> stream =
        pureAPI.listAllPersons().stream().map(PureAPIPerson::toContributor);
    String query = search.getQuery();
    if (query != null && !query.isEmpty()) {
      stream =
          stream.filter(
              item ->
                  item.getFirstName().toLowerCase().contains(query.toLowerCase())
                      || item.getLastName().toLowerCase().contains(query.toLowerCase())
                      || (item.getPersonId() != null
                          && item.getPersonId()
                              .getIdentifier()
                              .toLowerCase()
                              .contains(query.toLowerCase())));
    }
    result.setItems(stream.collect(Collectors.toList()));
    return result;
  }
}
