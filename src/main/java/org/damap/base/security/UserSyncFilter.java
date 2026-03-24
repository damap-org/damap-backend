package org.damap.base.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.damap.base.rest.auth.UserSyncService;

/**
 * Intercepts incoming REST requests to synchronize user data from the OIDC token (JWT) into the
 * local database. * Using a ContainerRequestFilter ensures this runs AFTER the multitenancy context
 * has been fully resolved by Quarkus.
 */
@Provider
@ApplicationScoped
public class UserSyncFilter implements ContainerRequestFilter {

  @Inject SecurityService securityService;

  @Inject UserSyncService userSyncService;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (securityService.isUserNotLoggedIn()) {
      return;
    }

    String userId = securityService.getUserId();

    if (userId != null) {
      String email = securityService.getEmail();
      String name = securityService.getUserName();
      String firstName = securityService.getGivenName();
      String lastName = securityService.getFamilyName();

      userSyncService.syncUser(userId, email, name, firstName, lastName);
    }
  }
}
