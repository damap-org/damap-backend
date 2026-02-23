package org.damap.base.rest.document.service;

import static org.reflections.Reflections.log;

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
import org.damap.base.rest.document.dto.MultipartBodyDTO;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "rest.gotenberg")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Timeout(10000)
public interface GotenbergRestService {

  @POST
  @Fallback(fallbackMethod = "fallback", skipOn = DamapApiException.class)
  @Path("/forms/libreoffice/convert")
  byte[] convertToPDF(MultipartBodyDTO datafile);

  @ClientExceptionMapper
  static DamapApiException toException(Response response) {
    // If the methods that caused the error are important for the error messages, you need a
    // ResteasyReactiveResponseExceptionMapper and register it as a provider
    return switch (response.getStatus()) {
      case 500, 502, 503, 504 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.GOTENBERG_NOT_AVAILABLE,
                  "The Gotenberg service is currently not available, caused by "
                      + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
      default ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.GOTENBERG_UNEXPECTED_ERROR,
                  "Something unexpected happened while calling the Gotenberg service, caused by "
                      + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
    };
  }

  default byte[] fallback(MultipartBodyDTO datafile) {
    log.info("The Gotenberg service did not respond and timed out");
    throw new DamapApiException(
        new ErrorDto(
            EErrorCode.GOTENBERG_NOT_AVAILABLE,
            "The Gotenberg service is currently not available, connection timed out"),
        Response.Status.INTERNAL_SERVER_ERROR);
  }
}
