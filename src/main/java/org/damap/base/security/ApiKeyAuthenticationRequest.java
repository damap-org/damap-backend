package org.damap.base.security;

import io.quarkus.security.identity.request.AuthenticationRequest;
import java.util.Map;

public class ApiKeyAuthenticationRequest implements AuthenticationRequest {
  private final String apiKey;

  public ApiKeyAuthenticationRequest(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getApiKey() {
    return apiKey;
  }

  @Override
  public <T> T getAttribute(String s) {
    return null;
  }

  @Override
  public void setAttribute(String s, Object o) {}

  @Override
  public Map<String, Object> getAttributes() {
    return Map.of();
  }
}
