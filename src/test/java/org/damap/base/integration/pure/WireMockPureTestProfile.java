package org.damap.base.integration.pure;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.HashMap;
import java.util.Map;

/** Test profile for PURE integration testing using WireMock HTTP. */
public class WireMockPureTestProfile implements QuarkusTestProfile {

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

    overrides.put("damap.tenant-aware.project-service", "elsevier-pure");
    overrides.put("damap.tenant-aware.elsevier-pure-backend", "http");

    overrides.put("damap.tenant-aware.elsevier-pure-endpoint-url", "http://localhost:8089");
    overrides.put("damap.tenant-aware.elsevier-pure-api-key", "test-api-key");

    overrides.put(
        "damap.tenant-aware.elsevier-pure-contributor-role-classifications[0].pure-role-uri",
        "/dk/atira/pure/member");
    overrides.put(
        "damap.tenant-aware.elsevier-pure-contributor-role-classifications[0].contributor-role",
        "PROJECT_MEMBER");
    overrides.put(
        "damap.tenant-aware.elsevier-pure-contributor-role-classifications[1].pure-role-uri",
        "/dk/atira/pure/test/projectlead");
    overrides.put(
        "damap.tenant-aware.elsevier-pure-contributor-role-classifications[1].contributor-role",
        "PROJECT_LEADER");

    overrides.put(
        "damap.tenant-aware.elsevier-pure-description-classification",
        "/dk/atira/pure/projectdescription");
    overrides.put(
        "damap.tenant-aware.elsevier-pure-project-lead-role-classification",
        "/dk/atira/pure/projectlead");

    overrides.put("damap.tenant-aware.person-services[0].display-text", "Pure");
    overrides.put("damap.tenant-aware.person-services[0].query-value", "PURE");
    overrides.put(
        "damap.tenant-aware.person-services[0].class-name",
        "org.damap.base.integration.pure.PurePersonService");

    System.out.println("WireMock PURE API configuration");

    return overrides;
  }
}
