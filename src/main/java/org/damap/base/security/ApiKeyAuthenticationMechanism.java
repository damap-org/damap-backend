package org.damap.base.security;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.damap.base.domain.User;
import org.damap.base.repo.ApiKeyRepo;

@ApplicationScoped
@Blocking
public class ApiKeyAuthenticationMechanism implements HttpAuthenticationMechanism {

  @Inject ApiKeyRepo apiKeyRepo;

  @Override
  public Uni<SecurityIdentity> authenticate(
      RoutingContext context, IdentityProviderManager identityProviderManager) {
    return Uni.createFrom()
        .emitter(
            em -> {
              String header = context.request().getHeader("Authorization");

              if (header != null && header.startsWith("ApiKey ")) {
                String apiKey = header.substring("ApiKey ".length());
                context
                    .vertx()
                    .executeBlocking(
                        () -> {
                          Optional<User> user = apiKeyRepo.findUserByApiKeyValue(apiKey);
                          if (user.isPresent()) {
                            AuthenticationRequest request = new ApiKeyAuthenticationRequest(apiKey);
                            identityProviderManager
                                .authenticate(request)
                                .subscribe()
                                .with(
                                    (identity) -> {
                                      em.complete(identity);
                                    },
                                    (failure) -> {
                                      em.fail(failure);
                                    });
                          } else {
                            em.complete(null);
                          }
                          return true;
                        });
              } else {
                em.complete(null);
              }
            });
  }

  @Override
  public Uni<ChallengeData> getChallenge(RoutingContext context) {
    return null;
  }
}
