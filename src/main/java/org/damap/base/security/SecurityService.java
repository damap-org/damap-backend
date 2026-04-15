package org.damap.base.security;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.quarkus.oidc.runtime.OidcJwtCallerPrincipal;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonArray;
import jakarta.json.JsonString;
import jakarta.ws.rs.core.HttpHeaders;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.rest.config.domain.TenantConfigResolver;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

/** SecurityService class. */
@JBossLog
@Unremovable
// @UnlessBuildProfile("test")
@ApplicationScoped
@DefaultBean
public class SecurityService {
  @Inject SecurityIdentity securityIdentity;

  @ConfigProperty(name = "damap.auth.user-id-claim")
  String userIdClaim;

  @ConfigProperty(name = "damap.auth.given-name-claim")
  String userGivenNameClaim;

  @ConfigProperty(name = "damap.auth.family-name-claim")
  String userFamilyNameClaim;

  @ConfigProperty(name = "damap.auth.email-claim")
  String emailClaim;

  @ConfigProperty(name = "damap.auth.admin-role-name")
  String adminRoleName;

  @ConfigProperty(name = "damap.auth.affiliations-claim")
  String affiliationsClaim;

  @ConfigProperty(name = "damap.tenants.tenant-list", defaultValue = "")
  Optional<List<String>> tenants;

  @ConfigProperty(name = "invenio.shared-secret")
  String sharedSecret;

  @Inject JWTParser parser;

  @Inject
  TenantConfigResolver tenantConfigResolver;

  /**
   * getUserId.
   *
   * @return a {@link java.lang.String} object
   */
  public String getUserId() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal)) return null;

    return ((OidcJwtCallerPrincipal) principal).getClaims().getClaimValue(userIdClaim).toString();
  }

  /**
   * getUserName.
   *
   * @return a {@link java.lang.String} object
   */
  public String getUserName() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal)) return null;
    return principal.getName();
  }

  public String getGivenName() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal)) return null;

    return getClaimValueAsString((OidcJwtCallerPrincipal) principal, userGivenNameClaim);
  }

  public String getFamilyName() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal)) return null;

    return getClaimValueAsString((OidcJwtCallerPrincipal) principal, userFamilyNameClaim);
  }

  public String getDisplayName() {
    String name = getUserName();
    if (name != null) {
      return name;
    }

    String firstName = getGivenName();
    String lastName = getFamilyName();
    if (firstName != null && lastName != null) {
      return firstName + " " + lastName;
    }

    return getEmail();
  }

  public String getEmail() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal)) return null;

    return getClaimValueAsString((OidcJwtCallerPrincipal) principal, emailClaim);
  }

  private String getClaimValueAsString(OidcJwtCallerPrincipal oidcPrincipal, String claimKey) {
    Object claimValue = oidcPrincipal.getClaims().getClaimValue(claimKey);
    return claimValue != null ? claimValue.toString() : null;
  }

  /**
   * isAdmin.
   *
   * @return a boolean
   */
  public boolean isAdmin() {
    return securityIdentity.hasRole(adminRoleName);
  }

  /**
   * Returns a String representing the affiliation of the logged-in user.
   *
   * @return a {@link String} representing the tenants' affiliation. Returns null if no affiliation
   *     could be read.
   */
  public String getAffiliation() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal oidcPrincipal) || isUserNotLoggedIn()) {
      return null;
    }

    if (tenantConfigResolver.isMultitenancyDisabled()) {
      return null;
    }

    if (oidcPrincipal.getClaims().getClaimValue(affiliationsClaim) == null) {
      throw new UnauthorizedException("Affiliation is required");
    }

    // affiliations come from the eduPersonScopedAffiliation attribute - in EduID they are
    // structured like role@institution
    // EduGain does not have this convention - currently we just throw an error if the affiliation
    // doesnt follow the EduID style
    // for more information see:
    // https://wiki.univie.ac.at/spaces/federation/pages/47025278/eduPersonScopedAffiliation
    List<String> affiliations =
        ((JsonArray) oidcPrincipal.getClaims().getClaimValue(affiliationsClaim))
            .stream()
                .map(JsonString.class::cast)
                .map(JsonString::getString)
                .map(
                    aff -> {
                      if (!aff.contains("@")) {
                        throw new UnauthorizedException(
                            "Affiliation is expected to include an @: " + aff);
                      }
                      return aff.split("@")[1].replace(".", "_");
                    })
                .distinct()
                .toList();

    List<String> tenantList = tenants.orElse(List.of());

    // check if affiliations are actual tenants and if exactly one unique affiliation is present
    // currently DAMAP cannot handle multiple affiliations
    List<String> validAffiliations = affiliations.stream().filter(tenantList::contains).toList();
    if (validAffiliations.size() == 1) {
      return validAffiliations.get(0);
    } else {
      throw new UnauthorizedException("Exactly one affiliation to a registered tenant is expected");
    }
  }

  /**
   * Validates a JWT token from the X-Auth header.
   *
   * @param headers HttpHeaders containing the Authorization token.
   * @return JsonWebToken if valid, null otherwise.
   */
  public JsonWebToken validateAuthHeader(HttpHeaders headers) {
    String jwtToken = headers.getHeaderString("X-Auth");

    if (jwtToken != null && !jwtToken.isEmpty()) {
      try {
        JsonWebToken jwt = parser.verify(jwtToken, sharedSecret);

        long exp = jwt.getExpirationTime();
        long currentTime = System.currentTimeMillis() / 1000;

        if (currentTime >= exp) throw new UnauthorizedException("Token expired.");

        return jwt;
      } catch (ParseException e) {
        log.error("Failed to parse JWT: ", e);
        return null;
      }
    }
    return null; // No Authorization header or not in the correct format
  }

  /**
   * Checks if the user is authorized based on the JWT token.
   *
   * @param headers HttpHeaders containing the Authorization token.
   * @return JsonWebToken if the user is authorized.
   */
  public JsonWebToken checkIfUserIsAuthorized(HttpHeaders headers) {
    JsonWebToken jwt = validateAuthHeader(headers);
    if (jwt == null) throw new UnauthorizedException("User unauthorized.");

    return jwt;
  }

  public boolean isUserNotLoggedIn() {
    return securityIdentity.isAnonymous();
  }
}
