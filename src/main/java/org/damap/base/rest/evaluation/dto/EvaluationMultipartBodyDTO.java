package org.damap.base.rest.evaluation.dto;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import org.jboss.resteasy.reactive.PartFilename;
import org.jboss.resteasy.reactive.PartType;

/** EvaluationMultipartBodyDTO class. */
public class EvaluationMultipartBodyDTO {

  @FormParam("maDMP")
  @PartType(MediaType.APPLICATION_JSON)
  @PartFilename("maDMP.json")
  public File maDMP;

  @FormParam("benchmark")
  @PartType(MediaType.TEXT_PLAIN)
  public String benchmark;
}
