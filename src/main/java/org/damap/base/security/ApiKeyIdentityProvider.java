package org.damap.base.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Optional;
import org.damap.base.domain.User;
import org.damap.base.domain.UserRole;
import org.damap.base.repo.ApiKeyRepo;

@ApplicationScoped
@Blocking
public class ApiKeyIdentityProvider implements IdentityProvider<ApiKeyAuthenticationRequest> {

  @Override
  public Class<ApiKeyAuthenticationRequest> getRequestType() {
    return ApiKeyAuthenticationRequest.class;
  }

  @Inject ApiKeyRepo apiKeyRepo;

  @Override
  public Uni<SecurityIdentity> authenticate(
      ApiKeyAuthenticationRequest request, AuthenticationRequestContext context) {
    String apiKey = request.getApiKey();

    Optional<User> userOptional = apiKeyRepo.findUserByApiKeyValue(apiKey);
    if (userOptional.isEmpty()) {
      throw new IllegalStateException("An error occurred while authenticating the API key");
    }

    User user = userOptional.get();

    QuarkusSecurityIdentity.Builder identity =
        QuarkusSecurityIdentity.builder()
            .setPrincipal(
                ApiKeyPrincipal.builder()
                    .displayName(user.getDisplayName())
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .build());

    for (UserRole role : user.getRoles()) {
      identity.addRole(role.getRole());
    }

    return Uni.createFrom().item(identity::build);
  }
}
