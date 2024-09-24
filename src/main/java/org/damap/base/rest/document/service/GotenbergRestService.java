package org.damap.base.rest.document.service;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.damap.base.rest.document.dto.MultipartBodyDTO;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "rest.gotenberg")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
@Consumes(MediaType.MULTIPART_FORM_DATA)
public interface GotenbergRestService {

  @POST
  @Path("/forms/libreoffice/convert")
  byte[] convertToPDF(MultipartBodyDTO datafile);
}
