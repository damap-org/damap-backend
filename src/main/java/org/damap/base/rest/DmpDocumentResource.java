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
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.rest.document.service.DocumentService;
import org.damap.base.security.SecurityService;
import org.damap.base.validation.AccessValidator;

/** DmpDocumentResource class. */
@Path("/api/document")
@Authenticated
@Produces(MediaType.APPLICATION_OCTET_STREAM)
@JBossLog
public class DmpDocumentResource {

  @Inject SecurityService securityService;

  @Inject AccessValidator accessValidator;

  @Inject ExportTemplateBroker exportTemplateBroker;

  @Inject DmpService dmpService;

  @Inject DocumentService documentService;

  /**
   * exportTemplate.
   *
   * @param dmpId a long
   * @param template a {@link org.damap.base.enums.ETemplateType} object
   * @return a {@link jakarta.ws.rs.core.Response} object
   */
  @GET
  @Path("/{dmpId}")
  @Deprecated(since = "4.3.0", forRemoval = true)
  public Response exportTemplate(
      @PathParam("dmpId") long dmpId, @QueryParam("template") ETemplateType template) {
    log.info("Return DMP document file for DMP with id=" + dmpId);

    return this.export(dmpId, template, true, "docx");
  }

  @GET
  @Path("/{dmpId}/export")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response export(
      @PathParam("dmpId") long dmpId,
      @QueryParam("template") ETemplateType template,
      @QueryParam("download") @DefaultValue("true") Boolean download,
      @QueryParam("filetype") @DefaultValue("docx") String filetype) {

    log.info("Returning DMP document file for DMP with id=" + dmpId);

    String personId = this.getPersonId();
    if (!accessValidator.canViewDmp(dmpId, personId)) {
      throw new ForbiddenException("Not authorized to access dmp with id " + dmpId);
    }

    StreamingOutput document;
    try {
      document = documentService.getExportDocument(dmpId, template, download, filetype);
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
  public ETemplateType getTemplateType(@PathParam("dmpId") long dmpId) {
    log.info("Return template type for DMP with id=" + dmpId);

    String personId = this.getPersonId();
    if (!accessValidator.canViewDmp(dmpId, personId)) {
      throw new ForbiddenException("Not authorized to access dmp with id " + dmpId);
    }

    return documentService.getTemplateType(dmpId);
  }

  private String getPersonId() {
    if (securityService == null) {
      throw new AuthenticationFailedException("User ID is missing.");
    }
    return securityService.getUserId();
  }
}
