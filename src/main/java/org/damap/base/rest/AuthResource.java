package org.damap.base.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.auth.domain.AuthDO;
import org.damap.base.rest.auth.service.AuthService;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

/** AuthResource class. */
@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@JBossLog
public class AuthResource {

  @Inject AuthService authService;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/jwt")
  public AuthDO getAuthJWT(@RequestBody AuthDO accessTokenDO) {
    log.info("Request to get DAMAP JWT from access token");
    return this.authService.getAuthDOFromAccessToken(accessTokenDO.getToken());
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/refresh")
  public AuthDO refreshAuthJWT(@RequestBody AuthDO oldJWTDO) {
    log.info("Attempting to refresh JWT");
    return this.authService.refreshJWTFromOldJWT(oldJWTDO.getToken());
  }
}
