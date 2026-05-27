package org.damap.base.rest.evaluation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.damap.base.rest.evaluation.EvaluationRemoteResource;
import org.damap.base.rest.evaluation.dto.BenchmarkDTO;
import org.damap.base.rest.evaluation.dto.EvaluationMultipartBodyDTO;
import org.damap.base.rest.evaluation.dto.EvaluationResultDTO;
import org.damap.base.rest.madmp.dto.Dmp;
import org.damap.base.rest.madmp.dto.MaDMPSchema11;
import org.damap.base.rest.madmp.service.MaDmpService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** EvaluationService class. */
@ApplicationScoped
@JBossLog
public class EvaluationService {

  @Inject @RestClient EvaluationRemoteResource evaluationRemoteResource;

  @Inject MaDmpService maDmpService;

  @Inject ObjectMapper mapper;

  /**
   * listBenchmarks.
   *
   * @return a {@link java.util.List} of {@link org.damap.base.rest.evaluation.dto.BenchmarkDTO}
   *     objects
   */
  public List<BenchmarkDTO> listBenchmarks() {
    return evaluationRemoteResource.listBenchmarks();
  }

  /**
   * assessBenchmark.
   *
   * @param dmpId a long
   * @param benchmark a {@link java.lang.String} object
   * @return a {@link java.util.List} of {@link
   *     org.damap.base.rest.evaluation.dto.EvaluationResultDTO} objects
   */
  public List<EvaluationResultDTO> assessBenchmark(long dmpId, String benchmark) {
    Dmp maDmp = maDmpService.getById(dmpId);
    MaDMPSchema11 schemaWrapper = new MaDMPSchema11();
    schemaWrapper.setDmp(maDmp);
    try {
      String jsonMaDmp = mapper.writeValueAsString(schemaWrapper);

      log.info("Sending maDMP to evaluation service for DMP ID: " + dmpId);
      EvaluationMultipartBodyDTO body = new EvaluationMultipartBodyDTO();
      body.maDMP = new ByteArrayInputStream(jsonMaDmp.getBytes(StandardCharsets.UTF_8));
      body.benchmark = benchmark;

      return evaluationRemoteResource.assessBenchmark(body);
    } catch (JsonProcessingException e) {
      log.error("Could not process maDMP with id: " + dmpId + " into String", e);
      throw new DamapApiException(
          new ErrorDto(
              EErrorCode.EVALUATION_UNEXPECTED_ERROR,
              "Error processing maDMP for evaluation, dmp id: " + dmpId),
          Response.Status.INTERNAL_SERVER_ERROR);
    }
  }
}
