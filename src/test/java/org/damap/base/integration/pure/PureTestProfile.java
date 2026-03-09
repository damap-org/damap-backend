package org.damap.base.integration.pure;

import com.google.common.io.Resources;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.HashMap;
import java.util.Map;

public class PureTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    HashMap<String, String> result = new HashMap<>();
    result.put("damap.tenant-aware.project-service", "elsevier-pure");
    result.put("damap.tenant-aware.person-services[0].display-text", "Pure");
    result.put("damap.tenant-aware.person-services[0].query-value", "PURE");
    result.put(
        "damap.tenant-aware.person-services[0].class-name",
        "org.damap.base.integration.pure.PurePersonService");

    result.put("damap.tenant-aware.elsevier-pure-backend", "file");
    result.put(
        "damap.tenant-aware.elsevier-pure-projects-file",
        Resources.getResource("org/damap/base/integration/pure/projects.json").toString());
    result.put(
        "damap.tenant-aware.elsevier-pure-persons-file",
        Resources.getResource("org/damap/base/integration/pure/persons.json").toString());

    result.put("damap.tenant-aware.elsevier-pure-endpoint-url", "http://localhost:12345/");
    result.put("damap.tenant-aware.elsevier-pure-api-key", "test-api-key");

    result.put("quarkus.http.test-port", "0");

    result.put(
        "damap.tenant-aware.elsevier-pure-description-classification",
        "/dk/atira/pure/projectdescription");
    result.put(
        "damap.tenant-aware.elsevier-pure-project-lead-role-classification",
        "/dk/atira/pure/projectlead");

    result.put(
        "damap.tenant-aware.elsevier-pure-contributor-role-classifications[0].pure-role-uri",
        "/dk/atira/pure/member");
    result.put(
        "damap.tenant-aware.elsevier-pure-contributor-role-classifications[0].contributor-role",
        "PROJECT_MEMBER");
    result.put(
        "damap.tenant-aware.elsevier-pure-contributor-role-classifications[1].pure-role-uri",
        "/dk/atira/pure/test/projectlead");
    result.put(
        "damap.tenant-aware.elsevier-pure-contributor-role-classifications[1].contributor-role",
        "PROJECT_LEADER");

    return result;
  }
}
