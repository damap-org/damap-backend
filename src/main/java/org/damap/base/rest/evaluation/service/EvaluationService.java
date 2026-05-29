package org.damap.base.rest.evaluation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.enums.EErrorCode;
import org.damap.base.exception.DamapApiException;
import org.damap.base.exception.ErrorDto;
import org.damap.base.rest.evaluation.EvaluationRemoteResource;
import org.damap.base.rest.evaluation.dto.BenchmarkDTO;
import org.damap.base.rest.evaluation.dto.EvaluationMultipartBodyDTO;
import org.damap.base.rest.evaluation.dto.EvaluationResultDTO;
import org.damap.base.rest.rda.service.RdaDmpService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/** EvaluationService class. */
@ApplicationScoped
@JBossLog
public class EvaluationService {

  @Inject @RestClient EvaluationRemoteResource evaluationRemoteResource;

  @Inject RdaDmpService rdaDmpService;

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
    File maDmpFile = null;
    try {
      String jsonMaDmp = mapper.writeValueAsString(rdaDmpService.getDMPDocument(dmpId));
      maDmpFile = File.createTempFile("dmp_" + dmpId + "_", ".json");
      Files.writeString(maDmpFile.toPath(), jsonMaDmp, StandardCharsets.UTF_8);

      EvaluationMultipartBodyDTO body = new EvaluationMultipartBodyDTO();
      body.maDMP = maDmpFile;
      body.benchmark = benchmark;

      return evaluationRemoteResource.assessBenchmark(body);
    } catch (IOException e) {
      log.error("Could not process maDMP with id: " + dmpId + " into JSON file", e);
      throw new DamapApiException(
          new ErrorDto(
              EErrorCode.EVALUATION_UNEXPECTED_ERROR,
              "Error processing maDMP for evaluation, dmp id: " + dmpId),
          Response.Status.INTERNAL_SERVER_ERROR);
    } finally {
      if (maDmpFile != null && maDmpFile.exists() && !maDmpFile.delete()) {
        log.warn("Failed to delete temporary maDMP file: " + maDmpFile.getAbsolutePath());
      }
    }
  }
}
