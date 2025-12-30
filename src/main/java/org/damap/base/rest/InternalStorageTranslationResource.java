package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.base.resource.*;
import org.damap.base.rest.storage.InternalStorageTranslationDO;
import org.damap.base.rest.storage.InternalStorageTranslationService;

@Path("/api/storages/{storageId}/translations")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@JBossLog
public class InternalStorageTranslationResource
    implements ResourceRead<InternalStorageTranslationDO>,
        ResourceCreate<InternalStorageTranslationDO, InternalStorageTranslationDO>,
        ResourceUpdate<InternalStorageTranslationDO, InternalStorageTranslationDO>,
        ResourceDelete {

  @Inject InternalStorageTranslationService internalStorageTranslationService;

  private static final String MESSAGE_START = "{\"message\":\"";

  @Override
  @RolesAllowed("${damap.auth.admin-role-name}")
  public InternalStorageTranslationDO create(@Valid InternalStorageTranslationDO data) {
    log.debug("Create internal storage translation");
    log.debug(data);

    try {
      return internalStorageTranslationService.create(data);
    } catch (ClientErrorException e) {
      throw new ClientErrorException(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(MESSAGE_START + e.getMessage() + "\"}")
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

  @Override
  @RolesAllowed("${damap.auth.admin-role-name}")
  public void delete(String id) {
    log.info("Delete internal storage translation with id " + id);
    try {
      internalStorageTranslationService.delete(id);
    } catch (NotFoundException e) {
      throw e;
    } catch (ClientErrorException e) {
      throw new ClientErrorException(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(MESSAGE_START + e.getMessage() + "\"}")
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

  @Override
  public InternalStorageTranslationDO read(String id, UriInfo uriInfo) {
    log.info("Read internal storage translation with id " + id);

    try {
      return internalStorageTranslationService.read(id, uriInfo.getQueryParameters());
    } catch (NumberFormatException e) {
      log.error("Invalid internal storage translation ID: " + id);
      throw new ClientErrorException(
          "Invalid internal storage translation ID: " + id + " - must be a number",
          Response.Status.BAD_REQUEST);
    }
  }

  @Override
  @RolesAllowed("${damap.auth.admin-role-name}")
  public InternalStorageTranslationDO update(String id, @Valid InternalStorageTranslationDO data) {
    log.info("Update internal storage translation with id " + id);
    log.info(data);
    try {
      return internalStorageTranslationService.update(id, data);
    } catch (NotFoundException e) {
      throw e;
    } catch (ClientErrorException e) {
      throw new ClientErrorException(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(MESSAGE_START + e.getMessage() + "\"}")
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

  @GET
  public List<InternalStorageTranslationDO> getAllByStorageId(@PathParam("storageId") String id) {
    log.info("getAll: " + id);

    return internalStorageTranslationService.getAllByStorageId(id);
  }
}
