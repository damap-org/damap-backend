package org.damap.base.integration.orcid;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.damap.base.integration.orcid.models.ORCIDExpandedSearchResult;
import org.damap.base.integration.orcid.models.ORCIDRecord;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;

/** OrcidPersonService interface. */
@RegisterRestClient(configKey = "rest.orcid.search")
@Path("/v3.0")
@Produces(MediaType.APPLICATION_JSON)
@Timeout(10000)
interface OrcidPersonService {

  Logger log = Logger.getLogger(OrcidPersonService.class);

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
  @Fallback(fallbackMethod = "getAllFallback", skipOn = DamapApiException.class)
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
  @Fallback(fallbackMethod = "getFallback", skipOn = DamapApiException.class)
  ORCIDRecord get(@PathParam(value = "orcid") String orcid);

  @ClientExceptionMapper
  static DamapApiException toException(Response response) {
    // If the methods that caused the error are important for the error messages, you need a
    // ResteasyReactiveResponseExceptionMapper and register it as a provider
    return switch (response.getStatus()) {
      case 404 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.ORCID_PERSON_NOT_FOUND, "A person couldn't be found with ORCID"),
              Response.Status.NOT_FOUND);
      case 500, 502, 503, 504 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.ORCID_NOT_AVAILABLE,
                  "ORCID is currently not available, caused by " + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
      default ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.ORCID_UNEXPECTED_ERROR,
                  "Something unexpected happened while calling ORCID, caused by "
                      + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
    };
  }

  default ORCIDExpandedSearchResult getAllFallback(String query, int rows) {
    throw fallback();
  }

  default ORCIDRecord getFallback(String orcid) {
    throw fallback();
  }

  default DamapApiException fallback() {
    log.info("The Orcid API did not respond and timed out");
    return new DamapApiException(
        new ErrorDto(
            EErrorCode.ORCID_NOT_AVAILABLE,
            "ORCID is currently not available, connection timed out"),
        jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
  }
}
