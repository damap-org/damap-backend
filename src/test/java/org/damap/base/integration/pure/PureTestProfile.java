package org.damap.base.integration.pure;

import com.google.common.io.Resources;
import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.HashMap;
import java.util.Map;

public class PureTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    HashMap<String, String> result = new HashMap<>();
    result.put("damap.projects-service", "elsevier-pure");
    result.put(
        "damap.person-services",
        "[{\"display-text\": \"Pure\", \"query-value\": \"PURE\", \"class-name\": \"org.damap.base.integration.pure.PurePersonService\"}]");

    result.put("damap.elsevier-pure-backend", "file");
    result.put(
        "damap.elsevier-pure-projects-file",
        Resources.getResource("org/damap/base/integration/pure/projects.json").toString());
    result.put(
        "damap.elsevier-pure-persons-file",
        Resources.getResource("org/damap/base/integration/pure/persons.json").toString());

    result.put("damap.pure-endpoint-url", "http://localhost:12345/");
    result.put("damap.elsevier-pure-endpoint-url", "http://localhost:12345/");
    result.put("damap.elsevier-pure-api-key", "test-api-key");
    result.put("quarkus.rest-client.elsevier-pure.url", "http://localhost:12345/");

    result.put("quarkus.http.test-port", "0");

    result.put("damap.project-service", "org.damap.base.integration.pure.PureProjectService");
    result.put(
        "damap.elsevier-pure-description-classification", "/dk/atira/pure/projectdescription");
    result.put(
        "damap.elsevier-pure-project-lead-role-classification", "/dk/atira/pure/projectlead");
    result.put(
        "damap.elsevier-pure-contributor-role-classifications",
        "{\"/dk/atira/pure/member\":\"PROJECT_MEMBER\", \"/dk/atira/pure/test/projectlead\":\"PROJECT_LEADER\"}");

    return result;
  }
}
