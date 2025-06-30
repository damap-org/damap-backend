package org.damap.base.integration.orcid;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.damap.base.integration.orcid.models.ORCIDExpandedSearchResult;
import org.damap.base.integration.orcid.models.ORCIDRecord;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/** OrcidPersonService interface. */
@RegisterRestClient(configKey = "rest.orcid.search")
@Path("/v3.0")
@Produces(MediaType.APPLICATION_JSON)
interface OrcidPersonService {

  /**
   * getAll.
   *
   * @param query a {@link java.lang.String} object
   * @param rows a int
   * @return a {@link ORCIDExpandedSearchResult} object
   */
  @Path("/expanded-search")
  @GET
  @ClientHeaderParam(name = "accept", value = MediaType.APPLICATION_JSON)
  ORCIDExpandedSearchResult getAll(@QueryParam("q") String query, @QueryParam("rows") int rows);

  /**
   * get.
   *
   * @param orcid a {@link java.lang.String} object
   * @return a {@link ORCIDRecord} object
   */
  @Path("/{orcid}/record")
  @GET
  @ClientHeaderParam(name = "accept", value = MediaType.APPLICATION_JSON)
  ORCIDRecord get(@PathParam(value = "orcid") String orcid);
}
