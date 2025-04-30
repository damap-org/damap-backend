package org.damap.base.rest;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.account.domain.ApiKeyDO;
import org.damap.base.rest.account.domain.UserPageDO;
import org.damap.base.rest.account.service.AccountService;
import org.damap.base.security.SecurityService;

@Path("/api/account")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@JBossLog
public class AccountResource {

  @Inject AccountService accountService;

  @Inject SecurityService securityService;

  @GET
  @Path("/api-keys")
  public List<String> getApiKeys() {
    log.info("GET /account/api-keys");
    String userId = securityService.getUserId();
    return accountService.getApiKeys(userId);
  }

  @POST
  @Path("/api-keys")
  public ApiKeyDO createApiKey(@QueryParam("keyName") String keyName) {
    log.info("POST /account/api-keys");
    String username = securityService.getUserId();
    String displayName = securityService.getDisplayName();
    String userId = securityService.getUserId();
    Set<String> roles = securityService.getUserRoles();
    return accountService.createApiKey(keyName, username, displayName, userId, roles);
  }

  @POST
  @Path("/api-keys/reset-all")
  @RolesAllowed("Damap Admin")
  public boolean resetAllApiKeys(@QueryParam("userId") String userId) {
    log.info("POST /account/api-keys/reset-all for userId: " + userId);
    return accountService.resetAllApiKeys(userId);
  }

  @POST
  @Path("/refresh-user-role")
  public void refreshUserApiKeyRole() {
    log.info("POST /account/refresh-user-role");
    String username = securityService.getUserId();
    String displayName = securityService.getDisplayName();
    String userId = securityService.getUserId();
    Set<String> roles = securityService.getUserRoles();
    accountService.refreshUserApiKeyRole(username, displayName, userId, roles);
  }

  @DELETE
  @Path("/api-keys/{key}")
  public void deleteApiKey(@PathParam("key") String key) {
    log.info("DELETE /account/api-keys/" + key);
    String userId = securityService.getUserId();
    accountService.deleteApiKey(key, userId);
  }

  @GET
  @Path("/users")
  @RolesAllowed("Damap Admin")
  public UserPageDO getAllUsers(
      @QueryParam("page") @DefaultValue("0") int page,
      @QueryParam("size") @DefaultValue("10") int size) {
    log.info("GET /account/users");
    int offset = page * size;
    return accountService.getAllUsers(offset, size);
  }
}
