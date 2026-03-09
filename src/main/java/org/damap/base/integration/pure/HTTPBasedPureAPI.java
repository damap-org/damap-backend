package org.damap.base.integration.pure;

import io.quarkus.arc.lookup.LookupIfProperty;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.enterprise.inject.Typed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;

@Path("")
@Produces(MediaType.APPLICATION_JSON)
@Typed(HTTPBasedPureAPI.class)
@LookupIfProperty(name = "damap.elsevier-pure-backend", stringValue = "http")
@Timeout(10000)
@RegisterClientHeaders(PureAuthenticationHeaderFactory.class)
interface HTTPBasedPureAPI extends PureAPI {

  Logger log = Logger.getLogger(HTTPBasedPureAPI.class);

  /**
   * List all projects using pagination.
   *
   * @param size the size of the page.
   * @param offset the offset to start at.
   * @return the response containing the projects on that page.
   */
  @GET
  @Fallback(fallbackMethod = "listAllProjectsFallback", skipOn = DamapApiException.class)
  @Path("/projects")
  @Override
  PureAPIPaginatedProjectsResponse listAllProjects(
      @QueryParam("size") Long size, @QueryParam("offset") Long offset);

  /**
   * Retrieve a project with a specific ID.
   *
   * @param uuid the ID of the project.
   * @return the project if found, or null if the project was not found.
   */
  @GET
  @Fallback(fallbackMethod = "getProjectFallback", skipOn = DamapApiException.class)
  @Path("/projects/{uuid}")
  @Override
  PureAPIProject getProject(@PathParam("uuid") String uuid);

  /**
   * Retrieve all persons in the database, paginated.
   *
   * @param size the size of the page.
   * @param offset the offset to start at.
   * @return the response containing the list of persons.
   */
  @GET
  @Path("/persons")
  @Fallback(fallbackMethod = "listAllPersonsFallback", skipOn = DamapApiException.class)
  @Override
  PureAPIPaginatedPersonsResponse listAllPersons(
      @QueryParam("size") Long size, @QueryParam("offset") Long offset);

  /**
   * Fetch a single person based on their ID.
   *
   * @param uuid the ID of the person to fetch.
   * @return the person if found, or null if the person was not found.
   */
  @GET
  @Path("/persons/{uuid}")
  @Fallback(fallbackMethod = "getPersonFallback", skipOn = DamapApiException.class)
  @Override
  PureAPIPerson getPerson(@PathParam("uuid") String uuid);

  @ClientExceptionMapper
  static DamapApiException toException(Response response) {
    // If the methods that caused the error are important for the error messages, you need a
    // ResteasyReactiveResponseExceptionMapper and register it as a provider
    return switch (response.getStatus()) {
      case 404 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.PURE_NOT_FOUND,
                  "A Person or Project over the Pure API couldn't be found"),
              Response.Status.NOT_FOUND);
      case 500, 502, 503, 504 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.PURE_NOT_AVAILABLE,
                  "Pure API is currently not available, caused by " + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
      default ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.PURE_UNEXPECTED_ERROR,
                  "Something unexpected happened while calling Pure API, caused by "
                      + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
    };
  }

  default PureAPIPaginatedProjectsResponse listAllProjectsFallback(Long size, Long offset) {
    throw fallback();
  }

  default PureAPIProject getProjectFallback(String uuid) {
    throw fallback();
  }

  default PureAPIPaginatedPersonsResponse listAllPersonsFallback(Long size, Long offset) {
    throw fallback();
  }

  default PureAPIPerson getPersonFallback(String uuid) {
    throw fallback();
  }

  default DamapApiException fallback() {
    log.info("The PURE API did not respond and timed out");
    return new DamapApiException(
        new ErrorDto(
            EErrorCode.PURE_NOT_AVAILABLE,
            "Pure API is currently not available, connection timed out"),
        jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
  }
}
