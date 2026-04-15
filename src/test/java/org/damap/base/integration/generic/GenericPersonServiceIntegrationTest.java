package org.damap.base.integration.generic;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.damap.base.rest.PersonServiceBroker;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestProfile(GenericTestProfile.class)
public class GenericPersonServiceIntegrationTest {

  private WireMockServer wireMockServer;

  @Inject PersonServiceBroker personServiceBroker;

  @ConfigProperty(name = "damap.generic-cris-api-key")
  String apiKey;

  @Test
  public void testGetPersonNoServiceUp() {
    var personService = personServiceBroker.getServiceForQueryParam("TEST");
    assert personService instanceof GenericPersonService
        : "Expected: GenericPersonService, got: " + personService.toString();

    try {
      personService.read("nonexistent");
      assert false : "No exception thrown";
    } catch (Exception ignored) {
    }
  }

  @Test
  public void testGetPerson() {
    var wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8888));
    wireMockServer.start();
    try {
      wireMockServer.stubFor(
          get(urlPathEqualTo("/persons/test-id"))
              .withHeader("Authorization", equalTo("Bearer test-api-key"))
              .willReturn(
                  aResponse()
                      .withStatus(200)
                      .withHeader("Content-Type", "application/json")
                      .withBody(
                          """
                                              {
                                                "id": "test-id",
                                                "contact_id": {
                                                  "identifier": "test-id",
                                                  "type": "other"
                                                },
                                                "mbox": "test@example.com",
                                                "name": "Test Contact"
                                              }
                                              """)));

      var personService = personServiceBroker.getServiceForQueryParam("TEST");
      assert personService instanceof GenericPersonService
          : "Expected: GenericPersonService, got: " + personService.toString();

      personService.read("test-id");

    } finally {
      wireMockServer.stop();
    }
  }
}
