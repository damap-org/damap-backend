package org.damap.base.integration.generic;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.HashMap;
import java.util.Map;

public class GenericTestProfile implements QuarkusTestProfile {
  @Override
  public Map<String, String> getConfigOverrides() {
    Map<String, String> overrides = new HashMap<>();

    overrides.put("damap.tenant-aware.generic-cris-url", "http://127.0.0.1:8888");
    overrides.put("damap.tenant-aware.generic-cris-api-key", "test-api-key");
    overrides.put(
        "damap.person-services",
        "[{\"display-text\": \"Test\", \"query-value\": \"TEST\", \"class-name\": \"org.damap.base.integration.generic.GenericPersonService\"}]'");

    return overrides;
  }
}
