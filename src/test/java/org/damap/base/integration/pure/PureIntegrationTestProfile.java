package org.damap.base.integration.pure;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.HashMap;
import java.util.Map;

/** Test profile for PURE integration testing using real Pure API credentials. */
public class PureIntegrationTestProfile implements QuarkusTestProfile {

  @Override
  public String getConfigProfile() {
    return "test";
  }

  @Override
  public Map<String, String> getConfigOverrides() {
    Map<String, String> overrides = new HashMap<>();

    overrides.put("quarkus.oidc.enabled", "false");
    overrides.put("quarkus.liquibase.migrate-at-start", "false");
    overrides.put("quarkus.hibernate-orm.database.generation", "none");

    overrides.put("quarkus.http.test-port", "0");

    overrides.put("damap.elsevier-pure-backend", "http");
    overrides.put(
        "damap.elsevier-pure-endpoint-url", "https://tugraz-staging.elsevierpure.com/ws/api");
    overrides.put("damap.elsevier-pure-api-key", "your-pure-api-key-here");
    overrides.put(
        "quarkus.rest-client.elsevier-pure.url", "https://tugraz-staging.elsevierpure.com/ws/api");

    System.out.println("PURE API configuration loaded for testing");

    return overrides;
  }
}
