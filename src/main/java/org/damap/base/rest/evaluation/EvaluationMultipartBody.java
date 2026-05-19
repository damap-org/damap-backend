package org.damap.base.rest.evaluation;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;
import org.jboss.resteasy.reactive.PartType;

public class EvaluationMultipartBody {

  @FormParam("maDMP")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public InputStream maDMP;

  @FormParam("benchmark")
  @PartType(MediaType.TEXT_PLAIN)
  public String benchmark;
}
