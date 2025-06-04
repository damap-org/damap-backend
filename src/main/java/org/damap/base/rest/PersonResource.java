package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.base.resource.ResourceRead;
import org.damap.base.rest.base.resource.ResourceSearch;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.persons.PersonService;

/** PersonResource class. */
@Path("/api/persons")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JBossLog
public class PersonResource implements ResourceRead<ContributorDO>, ResourceSearch<ContributorDO> {

  @Inject PersonServiceBroker personServiceBroker;

  /** {@inheritDoc} */
  @Override
  public ContributorDO read(String id, UriInfo uriInfo) {
    var queryParams = uriInfo.getQueryParameters();
    log.info("Return person details for id=" + id + " and query=" + queryParams.toString());
    PersonService searchService = personServiceBroker.getServiceFromQueryParams(queryParams);

    ContributorDO result = null;
    if (searchService != null) {
      result = searchService.read(id, queryParams);
    }

    return result;
  }

  /** {@inheritDoc} */
  @Override
  public ResultList<ContributorDO> search(UriInfo uriInfo) {
    var queryParams = uriInfo.getQueryParameters();
    log.info("Return person list for query=" + queryParams.toString());

    PersonService searchService = personServiceBroker.getServiceFromQueryParams(queryParams);
    Search search = Search.fromMap(queryParams);

    ResultList<ContributorDO> result = ResultList.fromItemsAndSearch(null, search);

    if (searchService != null) {
      result = searchService.search(queryParams);
    }

    return result;
  }
}
