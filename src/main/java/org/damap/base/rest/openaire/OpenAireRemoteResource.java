package org.damap.base.rest.openaire;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.damap.base.rest.openaire.domain.OpenAireSearchResponse;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;

/** OpenAireRemoteResource interface. */
@RegisterRestClient(configKey = "rest.openaire")
@Produces(MediaType.APPLICATION_JSON)
@Timeout(10000)
public interface OpenAireRemoteResource {

  Logger log = Logger.getLogger(OpenAireRemoteResource.class);

  /**
   * search.
   *
   * @param filter SKG-IF product filter
   * @param pageSize maximum number of matching products to return
   * @return an OpenAIRE Graph search response
   */
  @GET
  @Fallback(fallbackMethod = "fallbackResearchProducts", skipOn = DamapApiException.class)
  @Path("/graph/skg-if/v1/products")
  OpenAireSearchResponse searchResearchProducts(
      @QueryParam("filter") String filter, @QueryParam("page_size") int pageSize);

  @ClientExceptionMapper
  static DamapApiException toException(jakarta.ws.rs.core.Response response) {
    // If the methods that caused the error are important for the error messages, you need a
    // ResteasyReactiveResponseExceptionMapper and register it as a provider
    return switch (response.getStatus()) {
      case 404 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.OPENAIRE_NOT_FOUND, "A dataset couldn't be found with OpenAire"),
              jakarta.ws.rs.core.Response.Status.NOT_FOUND);
      case 500, 502, 503, 504 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.OPENAIRE_NOT_AVAILABLE,
                  "OpenAire is currently not available, caused by " + response.getStatus()),
              jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
      default ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.OPENAIRE_UNEXPECTED_ERROR,
                  "Something unexpected happened while calling OpenAire, caused by "
                      + response.getStatus()),
              jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
    };
  }

  default OpenAireSearchResponse fallbackResearchProducts(String filter, int pageSize) {
    log.info("The OpenAire Graph API did not respond and timed out");
    throw new DamapApiException(
        new ErrorDto(
            EErrorCode.OPENAIRE_NOT_AVAILABLE,
            "OpenAire is currently not available, connection timed out"),
        jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
  }
}
