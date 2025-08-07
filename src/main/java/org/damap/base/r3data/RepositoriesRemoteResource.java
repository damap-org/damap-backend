package org.damap.base.r3data;

import static org.reflections.Reflections.log;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;
import org.re3data.schema._2_2.Re3Data;

/** RepositoriesRemoteResource interface. */
@RegisterRestClient(configKey = "rest.r3data.repositories")
@Produces(MediaType.TEXT_XML)
@Timeout(10000)
public interface RepositoriesRemoteResource {

  /**
   * getAll.
   *
   * @return a {@link java.util.List} object
   */
  @GET
  @Fallback(fallbackMethod = "getAllFallback")
  @Path("/v1/repositories")
  generated.List getAll();

  /**
   * getById.
   *
   * @param id a {@link java.lang.String} object
   * @return a {@link org.re3data.schema._2_2.Re3Data} object
   */
  @GET
  @Fallback(fallbackMethod = "getByIdFallback")
  @Path("/v1/repository/{id}")
  Re3Data getById(@RestPath String id);

  /**
   * search.
   *
   * @param subjects a {@link java.util.List} object
   * @param contentTypes a {@link java.util.List} object
   * @param countries a {@link java.util.List} object
   * @param certificates a {@link java.util.List} object
   * @param pidSystems a {@link java.util.List} object
   * @param aidSystems a {@link java.util.List} object
   * @param repositoryAccess a {@link java.util.List} object
   * @param dataAccess a {@link java.util.List} object
   * @param dataUpload a {@link java.util.List} object
   * @param dataLicenses a {@link java.util.List} object
   * @param repositoryTypes a {@link java.util.List} object
   * @param institutionTypes a {@link java.util.List} object
   * @param versioning a {@link java.util.List} object
   * @param metadataStandards a {@link java.util.List} object
   * @return a {@link java.util.List} object
   */
  @GET
  @Fallback(fallbackMethod = "searchFallback")
  @Path("/beta/repositories")
  generated.List search(
      @RestQuery("subjects[]") List<String> subjects,
      @RestQuery("contentTypes[]") List<String> contentTypes,
      @RestQuery("countries[]") List<String> countries,
      @RestQuery("certificates[]") List<String> certificates,
      @RestQuery("pidSystems[]") List<String> pidSystems,
      @RestQuery("aidSystems[]") List<String> aidSystems,
      @RestQuery("repositoryAccess[]") List<String> repositoryAccess,
      @RestQuery("dataAccess[]") List<String> dataAccess,
      @RestQuery("dataUpload[]") List<String> dataUpload,
      @RestQuery("dataLicenses[]") List<String> dataLicenses,
      @RestQuery("repositoryTypes[]") List<String> repositoryTypes,
      @RestQuery("institutionTypes[]") List<String> institutionTypes,
      @RestQuery("versioning[]") List<String> versioning,
      @RestQuery("metadataStandards[]") List<String> metadataStandards);

  @ClientExceptionMapper
  static DamapApiException toException(Response response) {
    // If the methods that caused the error are important for the error messages, you need a
    // ResteasyReactiveResponseExceptionMapper and register it as a provider
    return switch (response.getStatus()) {
      case 404 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.RE3DATA_NOT_FOUND, "A Repository in Re3Data couldn't be found"),
              Response.Status.NOT_FOUND);
      case 500, 502, 503, 504 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.RE3DATA_NOT_AVAILABLE,
                  "Re3Data is currently not available, caused by " + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
      default ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.RE3DATA_UNEXPECTED_ERROR,
                  "Something unexpected happened while calling Re3Data, caused by "
                      + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
    };
  }

  default generated.List getAllFallback() {
    throw fallback();
  }

  default Re3Data getByIdFallback(String id) {
    throw fallback();
  }

  default generated.List searchFallback(
      List<String> subjects,
      List<String> contentTypes,
      List<String> countries,
      List<String> certificates,
      List<String> pidSystems,
      List<String> aidSystems,
      List<String> repositoryAccess,
      List<String> dataAccess,
      List<String> dataUpload,
      List<String> dataLicenses,
      List<String> repositoryTypes,
      List<String> institutionTypes,
      List<String> versioning,
      List<String> metadataStandards) {
    throw fallback();
  }

  default DamapApiException fallback() {
    log.info("The Re3data API did not respond and timed out");
    return new DamapApiException(
        new ErrorDto(
            EErrorCode.RE3DATA_NOT_AVAILABLE,
            "Re3data is currently not available, connection timed out"),
        jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR);
  }
}
