package org.damap.base.rest;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import java.util.Date;
import java.util.List;

import org.damap.base.rest.document.dto.ExportDocumentDTO;
import org.thymeleaf.context.Context;


import lombok.extern.jbosslog.JBossLog;
import org.damap.base.conversion.ExportTemplateBroker;
import org.damap.base.enums.EComplianceType;
import org.damap.base.enums.EDataKind;
import org.damap.base.enums.ETemplateType;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.rest.document.service.DocumentService;
import org.damap.base.security.SecurityService;
import org.damap.base.validation.AccessValidator;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/** DmpDocumentResource class. */
@Path("/api/document")
@Produces(MediaType.APPLICATION_OCTET_STREAM)
@JBossLog
public class DmpDocumentResource {

  @Inject SecurityService securityService;

  @Inject AccessValidator accessValidator;

  @Inject ExportTemplateBroker exportTemplateBroker;

  @Inject DmpService dmpService;

  @Inject DocumentService documentService;

  private final TemplateEngine templateEngine;

  public DmpDocumentResource() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

    // Tells the resolver to look inside the "templates" sub-folder
    resolver.setPrefix("templates/");

    // Appends ".html" to the template name we ask for
    resolver.setSuffix(".html");

    // Sets the template mode to HTML5
    resolver.setTemplateMode(TemplateMode.HTML);

    // Sets the character encoding
    resolver.setCharacterEncoding("UTF-8");

    // Create the engine and give it our resolver
    this.templateEngine = new TemplateEngine();
    this.templateEngine.setTemplateResolver(resolver);
  }
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
  @Path("/{dmpId}/export/do")
  public ExportDocumentDTO getExportDocument(
          @PathParam("dmpId") long dmpId
  ) {
    return this.documentService.getExportDocument(dmpId);
  }


  @GET
  @Path("/dmp/html")
  @Produces(MediaType.TEXT_HTML)
  public Response exportDmpAsHtml() {
    DmpDO dmp = createSampleDmp();

    // 1. Create a Thymeleaf "Context" to hold our variables.
    // This is the equivalent of the data map in the Quarkus extension.
    Context context = new Context();
    context.setVariable("dmp", dmp);

    // 2. Process the template using our manually created engine.
    // We provide the template name and the context with our data.
    String htmlContent = templateEngine.process("thymeleafTemplate", context);

    String fileName = "DMP-" + dmp.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".html";

    return Response.ok(htmlContent)
            .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
            .build();
  }

  // This helper method remains unchanged
  private DmpDO createSampleDmp() {
    DmpDO dmp = new DmpDO();
    dmp.setId(1L);
    dmp.setTitle("My Awesome Research Project DMP");
    dmp.setCreated(new Date());
    dmp.setModified(new Date());
    dmp.setDescription("This plan outlines the data management strategy.");
    dmp.setDataKind(EDataKind.SPECIFY);
    dmp.setDataGeneration("Data will be collected via satellite tracking.");
    dmp.setPersonalData(true);
    dmp.setPersonalDataCompliance(List.of(EComplianceType.INFORMED_CONSENT, EComplianceType.ANONYMISATION));
    return dmp;
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
