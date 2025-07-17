package org.damap.base.integration.mock;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.HashMap;
import java.util.Map;

/** Test profile for PURE integration testing using mock data. */
public class MockPureTestProfile implements QuarkusTestProfile {

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

    overrides.put("damap.projects-service", "elsevier-pure");
    overrides.put("damap.elsevier-pure-backend", "http");

    overrides.put("damap.elsevier-pure-endpoint-url", "http://localhost:12345/mock");
    overrides.put("quarkus.rest-client.elsevier-pure.url", "http://localhost:12345/mock");
    overrides.put("damap.elsevier-pure-api-key", "mock-api-key");

    overrides.put(
        "damap.elsevier-pure-contributor-role-classifications",
        "{\"/dk/atira/pure/member\":\"PROJECT_MEMBER\", \"/dk/atira/pure/projectlead\":\"PROJECT_LEADER\"}");

    overrides.put(
        "damap.elsevier-pure-description-classification", "/dk/atira/pure/projectdescription");
    overrides.put(
        "damap.elsevier-pure-project-lead-role-classification", "/dk/atira/pure/projectlead");

    System.out.println("Mock PURE API configuration loaded for testing");

    return overrides;
  }
}
