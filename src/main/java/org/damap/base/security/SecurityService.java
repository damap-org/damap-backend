package org.damap.base.security;

import io.quarkus.arc.DefaultBean;
import io.quarkus.arc.Unremovable;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
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

  @ConfigProperty(name = "damap.auth.internalUserId")
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
    if (!(principal instanceof DefaultJWTCallerPrincipal)) return null;

    return ((DefaultJWTCallerPrincipal) principal).getClaim(authUser).toString();
  }

  /**
   * getUserName.
   *
   * @return a {@link java.lang.String} object
   */
  public String getUserName() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof DefaultJWTCallerPrincipal)) return null;
    return ((DefaultJWTCallerPrincipal) principal).getName();
  }

  public String getDisplayName() {
    final Principal principal = securityIdentity.getPrincipal();
    if (!(principal instanceof DefaultJWTCallerPrincipal)) {
      return null;
    }

    DefaultJWTCallerPrincipal jwtCallerPrincipal = (DefaultJWTCallerPrincipal) principal;

    String name = getClaimValueAsString(jwtCallerPrincipal, "upn");
    if (name != null) {
      return name;
    }

    return getClaimValueAsString(jwtCallerPrincipal, "email");
  }

  private String getClaimValueAsString(
      DefaultJWTCallerPrincipal jwtCallerPrincipal, String claimKey) {
    Object claimValue = jwtCallerPrincipal.getClaim(claimKey);
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
