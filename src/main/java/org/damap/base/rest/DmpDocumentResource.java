package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.*;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.conversion.ExportTemplateBroker;
import org.damap.base.enums.ETemplateType;
import org.damap.base.exception.GlobalServerExceptionMapper;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.rest.document.service.DocumentService;
import org.damap.base.security.SecurityService;
import org.damap.base.validation.AccessValidator;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;

/** DmpDocumentResource class. */
@Path("/api/document")
@Authenticated
@Produces(MediaType.APPLICATION_OCTET_STREAM)
@JBossLog
@RegisterProvider(GlobalServerExceptionMapper.class)
public class DmpDocumentResource {

  @Inject SecurityService securityService;

  @Inject AccessValidator accessValidator;

  @Inject ExportTemplateBroker exportTemplateBroker;

  @Inject DmpService dmpService;

  @Inject DocumentService documentService;

  @GET
  @Path("/{dmpId}/export")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response export(
      @PathParam("dmpId") long dmpId,
      @QueryParam("template") Long templateId,
      @QueryParam("filetype") @DefaultValue("docx") String filetype) {

    log.info("Returning DMP document file for DMP with id=" + dmpId);

    String personId = this.getPersonId();
    if (!accessValidator.canViewDmp(dmpId, personId)) {
      throw new ForbiddenException("Not authorized to access dmp with id " + dmpId);
    }

    StreamingOutput document;
    try {
      document = documentService.getExportDocument(dmpId, templateId, filetype);
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      log.error("Error exporting DMP document", e);
      return Response.serverError().entity("Error exporting DMP document").build();
    }

    String filename = dmpService.getDefaultFileName(dmpId);

    // Return the PDF file in the response
    Response response =
        Response.ok(document)
            .header("Content-Disposition", "attachment;filename=" + filename + "." + filetype)
            .header("Access-Control-Expose-Headers", "Content-Disposition")
            .build();

    if (filetype.equals("pdf")) {
      response.getHeaders().add("Content-Type", "application/pdf");
    }

    return response;
  }

  @GET
  @Path("/{dmpId}/template_type")
  @Produces(MediaType.APPLICATION_JSON)
  public String getTemplateType(@PathParam("dmpId") long dmpId) {
    log.info("Return template type for DMP with id=" + dmpId);

    String personId = this.getPersonId();
    if (!accessValidator.canViewDmp(dmpId, personId)) {
      throw new ForbiddenException("Not authorized to access dmp with id " + dmpId);
    }

    String type = documentService.getTemplateType(dmpId);

    return "\"" + type + "\"";
  }

  private String getPersonId() {
    if (securityService == null) {
      throw new AuthenticationFailedException("User ID is missing.");
    }
    return securityService.getUserId();
  }
}
