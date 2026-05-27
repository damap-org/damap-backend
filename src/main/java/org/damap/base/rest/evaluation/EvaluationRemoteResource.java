package org.damap.base.rest.evaluation;

import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.damap.base.rest.evaluation.dto.BenchmarkDTO;
import org.damap.base.rest.evaluation.dto.EvaluationMultipartBodyDTO;
import org.damap.base.rest.evaluation.dto.EvaluationResultDTO;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.logging.Logger;

/** EvaluationRemoteResource interface. */
@RegisterRestClient(configKey = "rest.evaluation")
@Timeout(30000)
public interface EvaluationRemoteResource {

  Logger log = Logger.getLogger(EvaluationRemoteResource.class);

  /**
   * listBenchmarks.
   *
   * @return a {@link java.util.List} of {@link org.damap.base.rest.evaluation.dto.BenchmarkDTO}
   *     objects
   */
  @GET
  @Path("/benchmarks/list")
  @Produces(MediaType.APPLICATION_JSON)
  @Fallback(fallbackMethod = "listBenchmarksFallback", skipOn = DamapApiException.class)
  List<BenchmarkDTO> listBenchmarks();

  /**
   * assessBenchmark.
   *
   * @param data a {@link org.damap.base.rest.evaluation.dto.EvaluationMultipartBodyDTO} object
   * @return a {@link java.util.List} of {@link
   *     org.damap.base.rest.evaluation.dto.EvaluationResultDTO} objects
   */
  @POST
  @Path("/assess/benchmark")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  @Fallback(fallbackMethod = "assessBenchmarkFallback", skipOn = DamapApiException.class)
  List<EvaluationResultDTO> assessBenchmark(EvaluationMultipartBodyDTO data);

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

  default List<EvaluationResultDTO> assessBenchmarkFallback(EvaluationMultipartBodyDTO data) {
    log.info("The Evaluation API did not respond and timed out");
    throw new DamapApiException(
        new ErrorDto(
            EErrorCode.EVALUATION_NOT_AVAILABLE,
            "Evaluation service is currently not available, connection timed out"),
        Response.Status.INTERNAL_SERVER_ERROR);
  }
}
