package org.damap.base.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.Authenticated;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.ForbiddenException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.dmp.service.DmpService;
import org.damap.base.rest.madmp.dto.Dmp;
import org.damap.base.rest.madmp.service.MaDmpService;
import org.damap.base.security.SecurityService;
import org.damap.base.validation.AccessValidator;

/** MaDmpResource class. */
@Path("/api/madmp")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@JBossLog
public class MaDmpResource {

  @Inject SecurityService securityService;

  @Inject AccessValidator accessValidator;

  @Inject MaDmpService maDmpService;

  @Inject DmpService dmpService;

  /**
   * getById.
   *
   * @param id a long
   * @return a {@link org.damap.base.rest.madmp.dto.Dmp} object
   */
  @GET
  @Path("/{id}")
  public Dmp getById(@PathParam("id") long id) {
    log.info("Return maDMP for DMP with id: " + id);
    String personId = this.getPersonId();
    if (!accessValidator.canViewDmp(id, personId)) {
      throw new ForbiddenException("Not authorized to access dmp with id " + id);
    }
    return maDmpService.getById(id);
  }

  /**
   * getFileById.
   *
   * @param id a long
   * @return a {@link jakarta.ws.rs.core.Response} object
   */
  @GET
  @Path("/file/{id}")
  public Response getFileById(@PathParam("id") long id) {
    log.info("Return maDMP file for DMP with id: " + id);
    String personId = this.getPersonId();
    if (!accessValidator.canViewDmp(id, personId)) {
      throw new ForbiddenException("Not authorized to access dmp with id " + id);
    }

    String filename = dmpService.getDefaultFileName(id);
    Dmp maDMP = maDmpService.getById(id);
    String prettyMaDMP = maDMP.toString();
    ObjectMapper mapper = new ObjectMapper();
    try {
      prettyMaDMP = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(maDMP);
    } catch (JsonProcessingException e) {
      log.error("could not process maDMP with id: " + id + " into String");
    }
    Response.ResponseBuilder response = Response.ok(prettyMaDMP);
    response
        .header("Content-Disposition", "attachment; filename=" + filename + ".json")
        .header("Access-Control-Expose-Headers", "Content-Disposition");
    return response.build();
  }

  private String getPersonId() {
    if (securityService == null) {
      throw new AuthenticationFailedException("User ID is missing.");
    }
    return securityService.getUserId();
  }
}
