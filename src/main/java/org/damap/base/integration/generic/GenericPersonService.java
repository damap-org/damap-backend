package org.damap.base.integration.generic;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.integration.PersonService;
import org.damap.base.rda.dmpcommonstandard.ContributorMapper;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * This class provides person lookup against a generic REST person service as defined by the OpenAPI
 * spec in src/main/resources/org/damap/base/integration/rest/openapi.yaml
 */
@ApplicationScoped
@Priority(-1)
@JBossLog
public class GenericPersonService implements PersonService {
  // TODO add caching
  @Inject @RestClient Client client;

  private final ContributorMapper contributorMapper;

  public GenericPersonService() {
    this(new ContributorMapper(false));
  }

  public GenericPersonService(ContributorMapper contributorMapper) {
    this.contributorMapper = contributorMapper;
  }

  @Override
  public ContributorDO read(String id) {
    return contributorMapper.convertToContributor(client.getPerson(id));
  }

  @Override
  public ResultList<ContributorDO> search(Search query) {
    // TODO support client-side pagination here to avoid DoS-ing the backing service?
    return ResultList.fromItemsAndSearch(
        client.listAllPersons(query.getQuery()).stream()
            .map(contributorMapper::convertToContributor)
            .toList(),
        query);
  }
}
