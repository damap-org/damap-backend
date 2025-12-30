package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.resource.ResourceCreate;
import org.damap.base.rest.base.resource.ResourceDelete;
import org.damap.base.rest.base.resource.ResourceRead;
import org.damap.base.rest.base.resource.ResourceSearch;
import org.damap.base.rest.base.resource.ResourceUpdate;
import org.damap.base.rest.storage.InternalStorageDO;
import org.damap.base.rest.storage.InternalStorageService;

/** InternalStorageResource class. */
@Path("/api/storages")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@JBossLog
public class InternalStorageResource
    implements ResourceRead<InternalStorageDO>,
        ResourceCreate<InternalStorageDO, InternalStorageDO>,
        ResourceUpdate<InternalStorageDO, InternalStorageDO>,
        ResourceDelete,
        ResourceSearch<InternalStorageDO> {

  @Inject InternalStorageService internalStorageService;

  /**
   * create a new internal storage option.
   *
   * @param data a {@link org.damap.base.rest.storage.InternalStorageDO} object
   * @return a {@link org.damap.base.rest.storage.InternalStorageDO} object, with the ID set
   */
  @Override
  @POST
  @Path("")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("${damap.auth.admin-role-name}")
  public InternalStorageDO create(@Valid InternalStorageDO data) {
    log.debug("Create internal storage option");
    log.debug(data);

    try {
      return internalStorageService.create(data);
    } catch (ClientErrorException e) {
      throw new ClientErrorException(
          Response.status(Response.Status.BAD_REQUEST)
              .entity("{\"message\":\"" + e.getMessage() + "\"}")
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

  /**
   * read a specific internal storage option.
   *
   * @param id a {@link java.lang.String} object
   * @param uriInfo a {@link jakarta.ws.rs.core.UriInfo} object
   * @return a {@link org.damap.base.rest.storage.InternalStorageDO} object
   */
  @Override
  @GET
  @Path("/{id}")
  public InternalStorageDO read(@PathParam("id") String id, @Context UriInfo uriInfo) {
    log.debug("Read internal storage option with id " + id);

    try {
      return internalStorageService.read(id, uriInfo.getQueryParameters());
    } catch (NumberFormatException e) {
      log.error("Invalid internal storage ID: " + id);
      throw new ClientErrorException(
          Response.status(Response.Status.BAD_REQUEST)
              .entity(
                  "{\"message\":\"Invalid internal storage ID: " + id + " - must be a number\"}")
              .type(MediaType.APPLICATION_JSON)
              .build());
    }
  }

  /**
   * update an existing internal storage option.
   *
   * @param id a {@link java.lang.String} object representing the ID of the internal storage option
   *     to update
   * @param data a {@link org.damap.base.rest.storage.InternalStorageDO} object representing the new
   *     data
   * @return a {@link org.damap.base.rest.storage.InternalStorageDO} object, with the updated data
   */
  @Override
  @PUT
  @Path("/{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed("${damap.auth.admin-role-name}")
  public InternalStorageDO update(@PathParam("id") String id, @Valid InternalStorageDO data) {
    log.debug("Update internal storage option with id " + id);
    log.debug(data);
    return internalStorageService.update(id, data);
  }

  /**
   * delete an existing internal storage option.
   *
   * @param id a {@link java.lang.String} object, representing the ID of the internal storage option
   *     to delete
   */
  @Override
  @DELETE
  @RolesAllowed("${damap.auth.admin-role-name}")
  @Path("/{id}")
  public void delete(@PathParam("id") String id) {
    log.debug("Delete internal storage option with id " + id);
    internalStorageService.delete(id);
  }

  /**
   * search for internal storage options.
   *
   * @param uriInfo a {@link jakarta.ws.rs.core.UriInfo} object, representing the query parameters
   * @return a ResultList of {@link org.damap.base.rest.storage.InternalStorageDO} objects, that
   *     match the criteria
   */
  @Override
  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  public ResultList<InternalStorageDO> search(@Context UriInfo uriInfo) {
    log.debug("Search internal storage options");
    return internalStorageService.search(uriInfo.getQueryParameters());
  }
}
