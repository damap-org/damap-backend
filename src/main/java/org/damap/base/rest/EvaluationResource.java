package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.evaluation.dto.BenchmarkDTO;
import org.damap.base.rest.evaluation.dto.EvaluationResultDTO;
import org.damap.base.rest.evaluation.service.EvaluationService;
import org.damap.base.security.SecurityService;
import org.damap.base.validation.AccessValidator;

/** EvaluationResource class. */
@Path("/api/evaluation")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@JBossLog
public class EvaluationResource {

  @Inject SecurityService securityService;

  @Inject AccessValidator accessValidator;

  @Inject EvaluationService evaluationService;

  /**
   * getBenchmarks.
   *
   * @return a {@link java.util.List} of {@link org.damap.base.rest.evaluation.dto.BenchmarkDTO}
   *     objects
   */
  @GET
  @Path("/benchmarks")
  public List<BenchmarkDTO> getBenchmarks() {
    log.info("Return all benchmarks");
    return evaluationService.listBenchmarks();
  }

  /**
   * assessBenchmark.
   *
   * @param dmpId a long
   * @param benchmark a {@link java.lang.String} object
   * @return a {@link java.util.List} of {@link
   *     org.damap.base.rest.evaluation.dto.EvaluationResultDTO} objects
   */
  @POST
  @Path("/assess/{dmpId}")
  public List<EvaluationResultDTO> assessBenchmark(
      @PathParam("dmpId") long dmpId, @QueryParam("benchmark") String benchmark) {
    log.info("Run evaluation for DMP with id: " + dmpId + " and benchmark: " + benchmark);
    String personId = this.getPersonId();
    if (!accessValidator.canViewDmp(dmpId, personId)) {
      throw new ForbiddenException("Not authorized to access dmp with id " + dmpId);
    }
    return evaluationService.assessBenchmark(dmpId, benchmark);
  }

  private String getPersonId() {
    if (securityService == null) {
      throw new AuthenticationFailedException("User ID is missing.");
    }
    return securityService.getUserId();
  }
}
