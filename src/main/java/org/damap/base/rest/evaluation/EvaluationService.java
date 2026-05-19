package org.damap.base.rest.evaluation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.evaluation.dto.BenchmarkDTO;
import org.damap.base.rest.evaluation.dto.EvaluationResultDTO;
import org.damap.base.rest.madmp.dto.Dmp;
import org.damap.base.rest.madmp.dto.MaDMPSchema11;
import org.damap.base.rest.madmp.service.MaDmpService;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
@JBossLog
public class EvaluationService {

  @Inject @RestClient EvaluationRemoteResource evaluationRemoteResource;

  @Inject MaDmpService maDmpService;

  public List<BenchmarkDTO> listBenchmarks() {
    return evaluationRemoteResource.listBenchmarks();
  }

  public List<EvaluationResultDTO> assessBenchmark(long dmpId, String benchmark) {
    Dmp maDmp = maDmpService.getById(dmpId);
    MaDMPSchema11 schemaWrapper = new MaDMPSchema11();
    schemaWrapper.setDmp(maDmp);
    ObjectMapper mapper = new ObjectMapper();
    try {
      String jsonMaDmp = mapper.writeValueAsString(schemaWrapper);

      log.info("Sending maDMP to evaluation service for DMP ID: " + dmpId);
      // TODO: Remove
      log.info("maDMP JSON payload: " + jsonMaDmp);
      EvaluationMultipartBody body = new EvaluationMultipartBody();
      body.maDMP = new ByteArrayInputStream(jsonMaDmp.getBytes(StandardCharsets.UTF_8));
      body.benchmark = benchmark;

      return evaluationRemoteResource.assessBenchmark(body);
    } catch (JsonProcessingException e) {
      log.error("Could not process maDMP with id: " + dmpId + " into String", e);
      throw new RuntimeException("Error processing MaDMP", e);
    }
  }
}
