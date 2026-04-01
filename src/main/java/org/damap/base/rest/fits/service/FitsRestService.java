package org.damap.base.rest.fits.service;

import edu.harvard.fits.Fits;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.damap.base.rest.fits.dto.MultipartBodyDTO;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;

/** FitsRestService interface. */
@RegisterRestClient(configKey = "rest.fits")
@Produces(MediaType.APPLICATION_XML)
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Timeout(10000)
public interface FitsRestService {

  Logger log = Logger.getLogger(FitsRestService.class);

  /**
   * analyseFile.
   *
   * @param datafile a {@link org.damap.base.rest.fits.dto.MultipartBodyDTO} object
   * @return a {@link edu.harvard.fits.Fits} object
   */
  @POST
  @Fallback(fallbackMethod = "fallback", skipOn = DamapApiException.class)
  @Path("/examine")
  Fits analyseFile(MultipartBodyDTO datafile);

  @ClientExceptionMapper
  static DamapApiException toException(Response response) {
    // If the methods that caused the error are important for the error messages, you need a
    // ResteasyReactiveResponseExceptionMapper and register it as a provider
    return switch (response.getStatus()) {
      case 500, 502, 503, 504 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.FITS_NOT_AVAILABLE,
                  "The Fits service is currently not available, caused by " + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
      default ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.FITS_UNEXPECTED_ERROR,
                  "Something unexpected happened while calling the Fits service, caused by "
                      + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
    };
  }

  default Fits fallback(MultipartBodyDTO datafile) {
    log.info("The Fits service did not respond and timed out");
    throw new DamapApiException(
        new ErrorDto(
            EErrorCode.FITS_NOT_AVAILABLE,
            "The Fits service is currently not available, connection timed out"),
        Response.Status.INTERNAL_SERVER_ERROR);
  }
}
