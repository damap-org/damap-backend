package org.damap.base.security;

import java.security.Principal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiKeyPrincipal implements Principal {

  private String username;
  private String displayName;
  private String userId;

  @Override
  public String getName() {
    return username;
  }
}
