package org.damap.base.rest.auth.service;

import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.User;
import org.damap.base.repo.UserRepo;
import org.damap.base.rest.auth.domain.AuthDO;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

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

  @Inject JWTParser jwtParser;

  @Inject UserRepo userRepo;

  static final int TOKEN_POST_EXPIRATION_REFRESH_VALIDITY = 10800; // 3 hours in seconds

  @Transactional
  public AuthDO getAuthDOFromAccessToken(String accessToken) {
    try {
      Map<String, Object> claims;

      if (this.env.equals("PROD")) {
        claims = userInfoClient.getUserInfo("Bearer " + accessToken);
      } else {
        JwtConsumer consumer =
            new JwtConsumerBuilder().setSkipSignatureVerification().setSkipAllValidators().build();

        JwtClaims jwtClaims;

        try {
          jwtClaims = consumer.processToClaims(accessToken);
        } catch (Exception e) {
          // You potentially have a access token that is not a JWT, try fetching user info directly
          // Are you sure you actually are in non-PROD env if you get here?
          log.error(
              "Failed to parse access token as JWT, KeyCloak access tokens should be JWTs", e);
          log.error("Are you sure you actually are in non-PROD env if you get here?");
          throw new IllegalStateException("Failed to parse access token as JWT", e);
        }

        claims = new HashMap<>();
        claims.put("sub", jwtClaims.getSubject());
        String nickname = jwtClaims.getStringClaimValue("nickname");
        if (nickname == null || nickname.isEmpty()) {
          nickname = "unknown";
        }
        claims.put("nickname", nickname);

        String email = jwtClaims.getStringClaimValue("email");
        if (email == null || email.isEmpty()) {
          email = "unknown";
        }
        claims.put("email", email);
      }

      String sub = (String) claims.get("sub");

      User user = userRepo.findUserBySubject(sub);
      if (user == null) {
        user =
            User.builder()
                .subject(sub)
                .nickname((String) claims.get("nickname"))
                .email((String) claims.get("email"))
                .isAdmin(false)
                .build();
        userRepo.persist(user);
        log.info("Created new user with subject: " + sub);
      }

      String nickname = user.getNickname();
      String email = user.getEmail();

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
              .groups(roles)
              .expiresIn(Duration.ofHours(1))
              .sign();

      return AuthDO.builder().token(damapJWT).build();

    } catch (Exception e) {
      log.error("Failed to fetch user info", e);
      throw new WebApplicationException("Failed to fetch user info", 500);
    }
  }

  public AuthDO refreshJWTFromOldJWT(String oldJWT) {
    JsonWebToken jwt;
    try {
      JWTAuthContextInfo contextInfo = new JWTAuthContextInfo(publicKeyLocation, issuer);
      contextInfo.setExpGracePeriodSecs(TOKEN_POST_EXPIRATION_REFRESH_VALIDITY);
      jwt = jwtParser.parse(oldJWT, contextInfo);
    } catch (Exception e) {
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

    // Create new JWT with same claims
    String newJWT =
        Jwt.issuer(issuer)
            .subject(jwt.getSubject())
            .upn(jwt.getName())
            .claim("email", jwt.getClaim("email"))
            .groups(jwt.getGroups())
            .expiresIn(Duration.ofHours(1))
            .sign();

    return AuthDO.builder().token(newJWT).build();
  }
}
