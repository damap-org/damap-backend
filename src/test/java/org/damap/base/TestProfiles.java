package org.damap.base;

import io.quarkus.test.junit.QuarkusTestProfile;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestProfiles {
  public static class InvenioDAMAPEnabledProfile implements QuarkusTestProfile {

    /**
     * Returns additional config to be applied to the test. This will override any existing config
     * (including in application.(properties,yaml), however existing config will be merged with this
     * (i.e. application.(properties,yaml) config will still take effect, unless a specific config
     * key has been overridden).
     */
    @Override
    public Map<String, String> getConfigOverrides() {
      return Collections.singletonMap("invenio.disabled", "false");
    }
  }

  public static class DefaultProfile implements QuarkusTestProfile {

    // admin role name is configured in application.yaml under damap.auth.admin-role-name
    // but @TestSecurity cannot read from the application.yaml, so it needs to be hardcoded here
    public static final String ADMIN_ROLE = "Damap Admin";

    /** Makes sure that only the Mock services are used and not real systems like PURE */
    @Override
    public Map<String, String> getConfigOverrides() {
      Map<String, String> overrides = new HashMap<>();
      overrides.put("damap.projects-service", "default");
      return overrides;
    }

    @Override
    public String getConfigProfile() {
      return "test";
    }
  }
}
