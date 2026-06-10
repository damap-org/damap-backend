package org.damap.base.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.Authenticated;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rda.dmpcommonstandard.DMPDocument;
import org.damap.base.rest.dmp.domain.DmpDO;
import org.damap.base.rest.dmp.domain.DmpListItemDO;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.rest.rda.service.RdaDmpService;
import org.damap.base.security.SecurityService;
import org.damap.base.validation.AccessValidator;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/** DataManagementPlanResource class. */
@Path("/api/dmps")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@JBossLog
public class DataManagementPlanResource {

  @Inject SecurityService securityService;

  @Inject AccessValidator accessValidator;

  @Inject DmpService dmpService;

  @Inject RdaDmpService rdaDmpService;

  @Inject ObjectMapper objectMapper;

  private final String unauthorizedMessage(long id) {
    return "Not authorized to access dmp with id " + id;
  }

  // ADMIN

  /**
   * getAll.
   *
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/all")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public List<DmpListItemDO> getAll() {
    log.info("Return all Dmps");
    return dmpService.getAll();
  }

  /*@GET
  @Path("/person/{personId}")
  @RolesAllowed("${damap.auth.admin-role-name}")
  public List<DmpListItemDO> getDmpListByPerson(@RestPath String personId) {
      log.info("Return dmp for person id: " + personId);
      return dmpService.getDmpListByPersonId(personId);
  }*/

  // USER

  /**
   * getDmpList.
   *
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/list")
  public List<DmpListItemDO> getDmpList() {
    log.info("Return dmp list for user");
    String personId = this.getPersonId();
    log.info("User id: " + personId);
    return dmpService.getDmpListByPersonId(personId);
  }

  /*@GET
  @Path("/subordinates")
  @RolesAllowed("user")
  public List<DmpListItemDO> getDmpsSubordinates() {
      log.info("Return dmp list for subordinates");
      String personId = this.getPersonId();
      log.info("User id: " + personId);
      // TODO: Service stub
      return dmpService.getDmpListByPersonId(personId);
  }*/

  /**
   * getDmpById.
   *
   * @param id a {@link java.lang.String} object
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @GET
  @Path("/{id}")
  public DmpDO getDmpById(@RestPath String id) {
    log.info("Return dmp with id: " + id);
    String personId = this.getPersonId();
    long dmpId = Long.parseLong(id);
    if (!accessValidator.canViewDmp(dmpId, personId)) {
      throw new ForbiddenException(unauthorizedMessage(dmpId));
    }
    return dmpService.getDmpById(dmpId);
  }

  /**
   * saveDmp.
   *
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public DmpDO saveDmp(@Valid DmpDO dmpDO) {
    log.info("Save dmp");
    String personId = this.getPersonId();
    return dmpService.create(dmpDO, personId);
  }

  /**
   * updateDmp.
   *
   * @param id a {@link java.lang.String} object
   * @param dmpDO a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public DmpDO updateDmp(@RestPath String id, @Valid DmpDO dmpDO) {
    log.info("Update dmp with id: " + id);
    String personId = this.getPersonId();
    long dmpId = Long.parseLong(id);
    if (!accessValidator.canEditDmp(dmpId, personId)) {
      throw new ForbiddenException(unauthorizedMessage(dmpId));
    }
    return dmpService.update(dmpDO);
  }

  /**
   * deleteDmp.
   *
   * @param id a {@link java.lang.String} object
   */
  @DELETE
  @Path("/{id}")
  public void deleteDmp(@RestPath String id) {
    log.info("Delete dmp with id: " + id);
    String personId = this.getPersonId();
    long dmpId = Long.parseLong(id);
    if (!accessValidator.canDeleteDmp(dmpId, personId)) {
      throw new ForbiddenException(unauthorizedMessage(dmpId));
    }
    dmpService.delete(dmpId);
  }

  private String getPersonId() {
    if (securityService == null) {
      throw new AuthenticationFailedException("User ID is missing.");
    }
    return securityService.getUserId();
  }

  /**
   * getDmpByIdAndRevision.
   *
   * @param id a {@link java.lang.String} object
   * @param revision a long
   * @return a {@link org.damap.base.rest.dmp.domain.DmpDO} object
   */
  @GET
  @Path("/{id}/{revision}")
  public DmpDO getDmpByIdAndRevision(@RestPath String id, @RestPath long revision) {
    log.info("Return dmp with id: " + id + " and revision number: " + revision);
    String personId = this.getPersonId();
    long dmpId = Long.parseLong(id);
    if (!accessValidator.canViewDmp(dmpId, personId)) {
      throw new ForbiddenException(unauthorizedMessage(dmpId));
    }
    return dmpService.getDmpByIdAndRevision(dmpId, revision);
  }

  @POST
  @Path("/import")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response importDMP(@RestForm("file") FileUpload file) {
    log.info("Importing RDA DMP into DAMAP");

    if (file == null) {
      throw new BadRequestException("Missing JSON file. Upload it as multipart form field 'file'.");
    }

    DMPDocument rdaDmpDocument;
    try {
      rdaDmpDocument = objectMapper.readValue(file.uploadedFile().toFile(), DMPDocument.class);
    } catch (IOException e) {
      throw new BadRequestException("Invalid JSON file: " + e.getMessage(), e);
    }

    DmpDO createdDmp = rdaDmpService.importRdaDmp(rdaDmpDocument);

    return Response.status(Response.Status.CREATED).entity(createdDmp).build();
  }
}
