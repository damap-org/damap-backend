package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.access.domain.AccessDO;
import org.damap.base.rest.access.domain.UserDO;
import org.damap.base.rest.access.service.AccessService;
import org.damap.base.validation.AccessValidator;

/** AccessResource class. */
@Path("/api/access")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@JBossLog
public class AccessResource {

  @Inject AccessValidator accessValidator;
  @Inject AccessService accessService;

  /**
   * getAccessForDmp.
   *
   * @param id a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  @GET
  @Path("/dmps/{id}")
  public List<AccessDO> getAccessForDmp(@PathParam("id") String id) {
    log.info("Return access list for dmp with id: " + id);
    long dmpId = Long.parseLong(id);
    if (!this.accessValidator.canViewAccess(dmpId)) {
      throw new ForbiddenException();
    }
    return accessService.getByDmpId(dmpId);
  }

  /**
   * create.
   *
   * @param accessDO a {@link org.damap.base.rest.access.domain.AccessDO} object
   * @return a {@link org.damap.base.rest.access.domain.AccessDO} object
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public AccessDO create(@Valid AccessDO accessDO) {
    log.info("Create new access");
    // University id required for now, might change in the future
    if (accessDO.getIdentifier() == null) {
      throw new BadRequestException("University id is required");
    }

    if (!this.accessValidator.canCreateAccess(accessDO)) {
      throw new ForbiddenException();
    }
    return accessService.create(accessDO);
  }

  /**
   * delete.
   *
   * @param id a {@link java.lang.String} object
   */
  @DELETE
  @Path("/{id}")
  public void delete(@PathParam("id") String id) {
    log.info("Delete access with id: " + id);
    long accessId = Long.parseLong(id);
    if (!this.accessValidator.canDeleteAccess(accessId)) {
      throw new ForbiddenException();
    }
    accessService.delete(accessId);
  }

  @GET
  @Path("/user-search")
  public List<UserDO> searchUsers(@QueryParam("q") String query) {
    log.info("Search users with query: " + query);
    return accessService.searchUserDO(query);
  }
}
