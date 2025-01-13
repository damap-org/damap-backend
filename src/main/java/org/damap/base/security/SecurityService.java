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
import jakarta.ws.rs.core.HttpHeaders;
import java.security.Principal;
import lombok.extern.jbosslog.JBossLog;
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

  @ConfigProperty(name = "damap.auth.user")
  String authUser;

  @ConfigProperty(name = "invenio.shared-secret")
  String sharedSecret;

  @Inject JWTParser parser;

  /**
   * getUserId.
   *
   * @return a {@link java.lang.String} object
   */
  public String getUserId() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal)) return null;

    return ((OidcJwtCallerPrincipal) principal).getClaims().getClaimValue(authUser).toString();
  }

  /**
   * getUserName.
   *
   * @return a {@link java.lang.String} object
   */
  public String getUserName() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal)) return null;
    return ((OidcJwtCallerPrincipal) principal).getName();
  }

  public String getDisplayName() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof OidcJwtCallerPrincipal)) {
      return null;
    }

    OidcJwtCallerPrincipal oidcPrincipal = (OidcJwtCallerPrincipal) principal;

    String name = getClaimValueAsString(oidcPrincipal, "name");
    if (name != null) {
      return name;
    }

    String firstName = getClaimValueAsString(oidcPrincipal, "given_name");
    String lastName = getClaimValueAsString(oidcPrincipal, "family_name");
    if (firstName != null && lastName != null) {
      return firstName + " " + lastName;
    }

    return getClaimValueAsString(oidcPrincipal, "email");
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
    return securityIdentity.hasRole("Damap Admin");
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
}
