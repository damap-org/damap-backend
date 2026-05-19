package org.damap.base.rest.evaluation;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.damap.base.rest.evaluation.dto.BenchmarkDTO;
import org.damap.base.rest.evaluation.dto.EvaluationResultDTO;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.MultipartForm;

@RegisterRestClient(configKey = "rest.evaluation")
@Timeout(30000)
public interface EvaluationRemoteResource {

  Logger log = Logger.getLogger(EvaluationRemoteResource.class);

  @GET
  @Path("/benchmarks/list")
  @Produces(MediaType.APPLICATION_JSON)
  @Fallback(fallbackMethod = "listBenchmarksFallback", skipOn = DamapApiException.class)
  List<BenchmarkDTO> listBenchmarks();

  @POST
  @Path("/assess/benchmark")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Fallback(fallbackMethod = "assessBenchmarkFallback", skipOn = DamapApiException.class)
  List<EvaluationResultDTO> assessBenchmark(@MultipartForm EvaluationMultipartBody data);

  @ClientExceptionMapper
  static DamapApiException toException(Response response) {
    return switch (response.getStatus()) {
      case 500, 502, 503, 504 ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.EVALUATION_NOT_AVAILABLE,
                  "Evaluation service is currently not available, caused by "
                      + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
      default ->
          new DamapApiException(
              new ErrorDto(
                  EErrorCode.EVALUATION_UNEXPECTED_ERROR,
                  "Something unexpected happened while calling Evaluation service, caused by "
                      + response.getStatus()),
              Response.Status.INTERNAL_SERVER_ERROR);
    };
  }

  default List<BenchmarkDTO> listBenchmarksFallback() {
    log.info("The Evaluation API did not respond and timed out");
    throw new DamapApiException(
        new ErrorDto(
            EErrorCode.EVALUATION_NOT_AVAILABLE,
            "Evaluation service is currently not available, connection timed out"),
        Response.Status.INTERNAL_SERVER_ERROR);
  }

  default List<EvaluationResultDTO> assessBenchmarkFallback(EvaluationMultipartBody data) {
    log.info("The Evaluation API did not respond and timed out");
    throw new DamapApiException(
        new ErrorDto(
            EErrorCode.EVALUATION_NOT_AVAILABLE,
            "Evaluation service is currently not available, connection timed out"),
        Response.Status.INTERNAL_SERVER_ERROR);
  }
}
