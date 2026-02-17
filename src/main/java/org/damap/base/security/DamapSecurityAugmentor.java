package org.damap.base.security;

import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.damap.base.rest.auth.UserSyncService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This Security Augmentor intercepts the authentication process to synchronize user data from the
 * OIDC token (JWT) into the local database.
 *
 * <p>It extracts configurable user attributes (ID, email, name) from the principal's claims and
 * delegates to {@link UserSyncService} to ensure the user record exists and is up-to-date.
 */
@ApplicationScoped
public class DamapSecurityAugmentor {

  @Inject UserSyncService userSyncService;

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

  public Uni<SecurityIdentity> augment(
      SecurityIdentity identity, AuthenticationRequestContext context) {
    if (identity.isAnonymous()) {
      return Uni.createFrom().item(identity);
    }

    if (identity.getPrincipal() instanceof OidcJwtCallerPrincipal jwt) {

      String userId = jwt.getClaim(userIdClaim);
      String email = jwt.getClaim(emailClaim);
      String name = jwt.getClaim(nameClaim);
      String firstName = jwt.getClaim(givenNameClaim);
      String lastName = jwt.getClaim(familyNameClaim);

      return context.runBlocking(
          () -> {
            userSyncService.syncUser(userId, email, name, firstName, lastName);
            return identity;
          });
    }

    return Uni.createFrom().item(identity);
  }
}
