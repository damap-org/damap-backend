package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.exception.GlobalServerExceptionMapper;
import org.damap.base.rest.dmp.domain.DatasetDO;
import org.damap.base.rest.dmp.domain.MultipartBodyDO;
import org.damap.base.rest.dmp.mapper.DatasetDOMapper;
import org.damap.base.rest.file_analysis.service.FileAnalysisService;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;

@Path("/api/file-analysis")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.MULTIPART_FORM_DATA)
@JBossLog
@RegisterProvider(GlobalServerExceptionMapper.class)
public class FileAnalysisResource {

  @Inject FileAnalysisService fileAnalysisService;

  /**
   * examine.
   *
   * @param data a {@link org.damap.base.rest.dmp.domain.MultipartBodyDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.DatasetDO} object
   */
  @POST
  @Path("/examine")
  public DatasetDO examine(MultipartBodyDO data) {
    log.info("Analyse file");
    return DatasetDOMapper.mapEntityToDO(fileAnalysisService.analyseFile(data), new DatasetDO());
  }
}
