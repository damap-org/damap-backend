package org.damap.base.integration.mock;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** MockPersonRestService interface. */
@Path("")
@RegisterRestClient(configKey = "rest.mock-persons")
@Produces(MediaType.APPLICATION_JSON)
interface MockPersonRestService {

  /**
   * getContributorDetails.
   *
   * @param uniId a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/persons")
  List<ContributorDO> getContributorDetails(@QueryParam("universityId") String uniId);

  /**
   * getContributorSearchResult.
   *
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/persons")
  List<ContributorDO> getContributorSearchResult();
}
