package org.damap.base.rest.auth.service;

import io.quarkus.security.UnauthorizedException;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.User;
import org.damap.base.repo.UserRepo;
import org.damap.base.rest.auth.domain.AuthDO;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

@ApplicationScoped
@JBossLog
public class AuthService {

  @ConfigProperty(name = "mp.jwt.verify.publickey.issuer")
  String issuer;

  @Inject
  @ConfigProperty(name = "mp.jwt.verify.publickey.location")
  String publicKeyLocation;

  @Inject @RestClient UserInfoRestClient userInfoClient;

  @ConfigProperty(name = "damap.env")
  String env;

  @ConfigProperty(name = "damap.auth.method")
  String authMethod;

  // Configured claim names
  @ConfigProperty(name = "damap.auth.user", defaultValue = "sub")
  String userClaimName;

  @ConfigProperty(name = "damap.auth.claims.email", defaultValue = "email")
  String emailClaimName;

  @ConfigProperty(name = "damap.auth.claims.nickname", defaultValue = "name")
  String nicknameClaimName;

  @ConfigProperty(name = "damap.auth.claims.groups", defaultValue = "groups")
  String groupsClaimName;

  @ConfigProperty(name = "damap.auth.admin-role-name", defaultValue = "Damap Admin")
  String adminRoleName;

  @ConfigProperty(name = "damap.auth.affiliation-name", defaultValue = "affiliation")
  String affiliationName;

  @Inject JWTParser jwtParser;

  @Inject UserRepo userRepo;

  static final int TOKEN_POST_EXPIRATION_REFRESH_VALIDITY = 10800; // 3 hours in seconds

  @Inject
  @ConfigProperty(name = "damap.auth.keycloakJwkUrl", defaultValue = "unused")
  String keycloakJwkUrl;

  @Inject JsonWebToken currentToken;

  /**
   * Get AuthDO containing DAMAP JWT from access token. Either fetch user info from user info
   * endpoint (federation) or parse JWT access token (keycloak).
   *
   * <p>A user entry is created in the database if it does not exist. An internal DAMAP JWT is
   * created and returned.
   *
   * @param accessToken Access token from external identity provider.
   * @return AuthDO containing DAMAP JWT.
   */
  @Transactional
  public AuthDO getAuthDOFromAccessToken(String accessToken) {

    Map<String, String> claims;
    boolean isAdmin = false;

    if (this.authMethod.equals("federation")) {
      // Just call the user info endpoint to get user claims
      try {
        claims =
            userInfoClient.getUserInfo("Bearer " + accessToken).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue())));
      } catch (Exception e) {
        log.warn("Failed to fetch user info from user info endpoint: " + e.getMessage());
        throw new NotAuthorizedException("Invalid or expired access token", "Bearer");
      }
    } else if (this.authMethod.equals("keycloak")) {
      // Parse the access token as JWT to get user claims
      HttpsJwks httpsJwks = new HttpsJwks(keycloakJwkUrl);
      HttpsJwksVerificationKeyResolver jwksResolver =
          new HttpsJwksVerificationKeyResolver(httpsJwks);

      JwtConsumer consumer =
          new JwtConsumerBuilder()
              .setExpectedAudience("damap")
              .setVerificationKeyResolver(jwksResolver)
              .build();

      JwtClaims jwtClaims;

      try {
        jwtClaims = consumer.processToClaims(accessToken);
      } catch (InvalidJwtException e) {
        log.warn("Failed to parse/verify JWT: " + e.getMessage());
        throw new NotAuthorizedException("Invalid or expired access token", "Bearer");
      } catch (Exception e) {
        // You potentially have a access token that is not a JWT, try fetching user info directly
        // Are you sure you actually configured correctly the authMethod if you get here?
        log.error("Failed to parse access token as JWT, KeyCloak access tokens should be JWTs", e);
        log.error("Unexpected error while parsing JWT access token", e);
        throw new WebApplicationException("Failed to parse access token", 500);
      }

      try {
        claims = new HashMap<>();
        claims.put("sub", jwtClaims.getStringClaimValue(this.userClaimName));
        String nickname = jwtClaims.getStringClaimValue(this.nicknameClaimName);
        if (nickname == null || nickname.isEmpty()) {
          nickname = "unknown";
        }
        claims.put("nickname", nickname);

        String email = jwtClaims.getStringClaimValue(this.emailClaimName);
        if (email == null || email.isEmpty()) {
          email = "unknown";
        }
        claims.put("email", email);

        // Check if "groups" has the admin role
        Object groupsObj = jwtClaims.getClaimValue(this.groupsClaimName);
        if (groupsObj instanceof List) {
          List<?> groupsList = (List<?>) groupsObj;
          for (Object group : groupsList) {
            if (this.adminRoleName.equals(group)) {
              isAdmin = true;
              break;
            }
          }
        }

        claims.put("affiliation", "[user@" + this.affiliationName + "]");
      } catch (org.jose4j.jwt.MalformedClaimException e) {
        log.error("Malformed claim in JWT: " + e.getMessage());
        throw new WebApplicationException("Malformed claim in access token", 400);
      }
    } else {
      throw new IllegalStateException("Unsupported auth method: " + this.authMethod);
    }

    // Logic to extract the affiliation domain, needed for multitenant support
    String sub = claims.get("sub");

    String rawAffiliation = claims.get("affiliation");
    if (rawAffiliation == null || rawAffiliation.isEmpty()) {
      throw new UnauthorizedException("Affiliation claim is missing in the token");
    }

    String cleanedAffiliation = rawAffiliation.replace("[", "").replace("]", "").trim();
    if (cleanedAffiliation.contains(",")) {
      cleanedAffiliation = cleanedAffiliation.split(",")[0].trim();
    }

    int atIndex = cleanedAffiliation.lastIndexOf('@');
    if (atIndex == -1 || atIndex == cleanedAffiliation.length() - 1) {
      throw new IllegalArgumentException("Affiliation claim is malformed: " + rawAffiliation);
    }

    String affiliationDomain = cleanedAffiliation.substring(atIndex + 1);

    // Check if user exists in the database, if not create it
    User user = userRepo.findUserBySubject(sub);
    if (user == null) {
      user =
          User.builder()
              .subject(sub)
              .nickname(claims.get("nickname"))
              .email(claims.get("email"))
              .isAdmin(isAdmin)
              .build();
      userRepo.persist(user);
      log.info("Created new user with subject: " + sub);

      /*
      TODO: Verify in DMP Access if one of the old IDs match the current
       uid from the claims (for the SATOSA case), if yes, write migration
       logic for the access from the old user to the new one.
       */
    } else {
      // Update user info if changed, we on purpose do not update isAdmin here
      // as we consider our DB after the first creation the source of truth for admin status
      boolean changed = false;
      if (!Objects.equals(user.getEmail(), claims.get("email"))) {
        user.setEmail(claims.get("email"));
        changed = true;
      }
      if (!Objects.equals(user.getNickname(), claims.get("nickname"))) {
        user.setNickname(claims.get("nickname"));
        changed = true;
      }
      if (changed) {
        userRepo.persist(user);
      }
    }

    String nickname = user.getNickname();
    String email = user.getEmail();

    // Build the roles, we check if the user is admin from the database entry
    // This allows us to manage admin rights internally
    Set<String> roles = new HashSet<>();
    roles.add("default-roles-damap");
    if (user.isAdmin()) {
      roles.add("Damap Admin");
    }

    String damapJWT =
        Jwt.issuer(issuer)
            .subject(sub)
            .upn(nickname)
            .claim("email", email)
            .claim("affiliation", affiliationDomain)
            .claim("can_be_refreshed", true)
            .groups(roles)
            .expiresIn(Duration.ofHours(1))
            .sign();

    return AuthDO.builder().token(damapJWT).build();
  }

  /**
   * Get affiliation of the currently authenticated user from the JWT token.
   *
   * @return Affiliation string.
   */
  public String getAffiliationOfAuthenticatedUser() {
    String affiliation = currentToken.getClaim("affiliation");

    if (affiliation == null) {
      return "unknown";
    }
    return affiliation;
  }

  /**
   * Refresh JWT from old JWT if it is within the allowed refresh period.
   *
   * @param oldJWT Old JWT token.
   * @return New AuthDO with refreshed JWT.
   */
  public AuthDO refreshJWTFromOldJWT(String oldJWT) {
    JsonWebToken jwt;
    try {
      JWTAuthContextInfo contextInfo = new JWTAuthContextInfo(publicKeyLocation, issuer);
      contextInfo.setExpGracePeriodSecs(TOKEN_POST_EXPIRATION_REFRESH_VALIDITY);
      jwt = jwtParser.parse(oldJWT, contextInfo);
    } catch (ParseException e) {
      log.error("Failed to parse old JWT", e);
      throw new WebApplicationException("Invalid old JWT, re-authentication required", 401);
    }

    // Check if expiration is less than TOKEN_POST_EXPIRATION_REFRESH_VALIDITY seconds ago
    if ((jwt.getExpirationTime() + TOKEN_POST_EXPIRATION_REFRESH_VALIDITY)
        < (System.currentTimeMillis() / 1000)) {
      log.info(
          jwt.getExpirationTime()
              + (TOKEN_POST_EXPIRATION_REFRESH_VALIDITY * 1000)
              + " < "
              + (System.currentTimeMillis() / 1000));
      throw new WebApplicationException(
          "Old JWT expired too long ago, re-authentication required", 401);
    }

    // Check if the JWT can be refreshed, a JWT can only be refreshed once
    // otherwise infinite refresh loops could occur
    if (!Boolean.TRUE.equals(jwt.getClaim("can_be_refreshed"))) {
      throw new WebApplicationException(
          "Old JWT cannot be refreshed, re-authentication required", 401);
    }

    // Create new JWT with same claims
    String newJWT =
        Jwt.issuer(issuer)
            .subject(jwt.getSubject())
            .upn(jwt.getName())
            .claim("email", jwt.getClaim("email"))
            .claim("affiliation", jwt.getClaim("affiliation"))
            .claim("can_be_refreshed", false)
            .groups(jwt.getGroups())
            .expiresIn(Duration.ofHours(1))
            .sign();

    return AuthDO.builder().token(newJWT).build();
  }
}
