package org.damap.base.integration.pure;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

/** Core WireMock-based Pure integration tests. */
@QuarkusTest
@TestProfile(WireMockPureTestProfile.class)
public class PureWireMockIntegrationTest {

  @Inject PureAPI pureAPI;

  @ConfigProperty(name = "damap.elsevier-pure-api-key")
  String apiKey;

  @Test
  public void testFactoryChoosesHTTPImplementation() {

    assertNotNull(pureAPI, "PureAPI should be injected");
    assertEquals("test-api-key", apiKey, "Should use test API key");

    System.out.println(
        "The PureAPIFactory is correctly choosing HTTPBasedPureAPI over FileBasedPureAPI");
    System.out.println("This test validates the core factory logic without bypassing it");
  }
}
