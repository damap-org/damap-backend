package org.damap.base.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.damap.base.rest.auth.UserSyncService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * Intercepts incoming REST requests to synchronize user data from the OIDC token (JWT) into the
 * local database. * Using a ContainerRequestFilter ensures this runs AFTER the multitenancy context
 * has been fully resolved by Quarkus.
 */
@Provider
@ApplicationScoped
public class UserSyncFilter implements ContainerRequestFilter {

  @Inject UserSyncService userSyncService;

  @Inject JsonWebToken jwt;

  @ConfigProperty(name = "damap.auth.user-id-claim", defaultValue = "sub")
  String userIdClaim;

  @ConfigProperty(name = "damap.auth.email-claim", defaultValue = "email")
  String emailClaim;

  @ConfigProperty(name = "damap.auth.name-claim", defaultValue = "name")
  String nameClaim;

  @ConfigProperty(name = "damap.auth.given-name-claim", defaultValue = "given_name")
  String givenNameClaim;

  @ConfigProperty(name = "damap.auth.family-name-claim", defaultValue = "family_name")
  String familyNameClaim;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (jwt == null || jwt.getRawToken() == null) {
      return;
    }

    String userId = jwt.getClaim(userIdClaim);

    if (userId != null) {
      String email = jwt.getClaim(emailClaim);
      String name = jwt.getClaim(nameClaim);
      String firstName = jwt.getClaim(givenNameClaim);
      String lastName = jwt.getClaim(familyNameClaim);

      userSyncService.syncUser(userId, email, name, firstName, lastName);
    }
  }
}
