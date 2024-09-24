package org.damap.base.rest.document.dto;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.core.MediaType;
import java.io.File;
import org.jboss.resteasy.reactive.PartType;

/** MultipartBodyDTO class. */
public class MultipartBodyDTO {

  @FormParam("files")
  @PartType(MediaType.APPLICATION_OCTET_STREAM)
  public File file;
}
