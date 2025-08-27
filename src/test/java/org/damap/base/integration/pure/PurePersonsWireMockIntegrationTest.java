package org.damap.base.integration.pure;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import java.net.URI;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * WireMock-based Pure Persons integration tests. Tests the real PureAPIFactory → HTTPBasedPureAPI →
 * Pure Persons API flow.
 */
@QuarkusTest
@TestProfile(WireMockPureTestProfile.class)
public class PurePersonsWireMockIntegrationTest {

  @ConfigProperty(name = "damap.elsevier-pure-api-key")
  String apiKey;

  private WireMockServer wireMockServer;
  private HTTPBasedPureAPI pureAPI;

  @BeforeEach
  public void setup() {
    wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
    wireMockServer.start();

    String wireMockUrl = "http://localhost:" + wireMockServer.port();
    System.out.println("WireMock server started at: " + wireMockUrl);

    pureAPI =
        RestClientBuilder.newBuilder()
            .baseUri(URI.create(wireMockUrl))
            .build(HTTPBasedPureAPI.class);
  }

  @AfterEach
  public void teardown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  public void testHTTPPersonsEndpoint() {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/persons"))
            .withHeader("api-key", equalTo("test-api-key"))
            .withQueryParam("size", equalTo("10"))
            .withQueryParam("offset", equalTo("0"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createMockPersonsResponse())));

    PureAPIPaginatedPersonsResponse response = pureAPI.listAllPersons(10L, 0L);

    assertNotNull(response, "Response should not be null");
    assertNotNull(response.getItems(), "Items should not be null");
    assertEquals(2, response.getItems().size(), "Should have 2 persons from test data");

    PureAPIPerson firstPerson = response.getItems().get(0);
    assertEquals("a8864c16-5264-4d94-955d-8be7e00f26a9", firstPerson.getUuid());
    assertEquals("Jane", firstPerson.getName().getFirstName());
    assertEquals("Doe", firstPerson.getName().getLastName());

    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/persons"))
            .withHeader("api-key", equalTo("test-api-key"))
            .withQueryParam("size", equalTo("10"))
            .withQueryParam("offset", equalTo("0")));
  }

  @Test
  public void testHTTPSinglePersonEndpoint() {
    String personUuid = "a8864c16-5264-4d94-955d-8be7e00f26a9";

    wireMockServer.stubFor(
        get(urlPathEqualTo("/persons/" + personUuid))
            .withHeader("api-key", equalTo("test-api-key"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createMockSinglePersonResponse())));

    PureAPIPerson person = pureAPI.getPerson(personUuid);

    assertNotNull(person, "Person should not be null");
    assertEquals(personUuid, person.getUuid());
    assertEquals("Jane", person.getName().getFirstName());
    assertEquals("Doe", person.getName().getLastName());
    assertEquals("0000-0001-2345-6789", person.getOrcid());

    // Verify WireMock received the request
    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/persons/" + personUuid))
            .withHeader("api-key", equalTo("test-api-key")));
  }

  @Test
  public void testPersonsAPIKeyAuthenticationFailure() {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/persons"))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\": \"Unauthorized\"}")));

    assertThrows(
        Exception.class,
        () -> {
          pureAPI.listAllPersons(10L, 0L);
        },
        "Should throw exception for unauthorized access");

    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/persons"))
            .withQueryParam("size", equalTo("10"))
            .withQueryParam("offset", equalTo("0")));
  }

  @Test
  public void testPersonDataValidation() {
    String personUuid = "a8864c16-5264-4d94-955d-8be7e00f26a9";

    // single person endpoint
    wireMockServer.stubFor(
        get(urlPathEqualTo("/persons/" + personUuid))
            .withHeader("api-key", equalTo("test-api-key"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createMockSinglePersonResponse())));

    PureAPIPerson purePerson = pureAPI.getPerson(personUuid);

    assertNotNull(purePerson, "Pure API should return person");

    System.out.println("=== PURE PERSONS API DATA ===");
    System.out.println("Person UUID: " + purePerson.getUuid());
    System.out.println(
        "Person Name: "
            + purePerson.getName().getFirstName()
            + " "
            + purePerson.getName().getLastName());
    System.out.println("Person ORCID: " + purePerson.getOrcid());
    System.out.println("Person Email: " + purePerson.getEmail());

    assertNotNull(purePerson.getName(), "Person should have a name");
    assertNotNull(purePerson.getName().getFirstName(), "Person should have first name");
    assertNotNull(purePerson.getName().getLastName(), "Person should have last name");
    assertNotNull(purePerson.getOrcid(), "Person should have ORCID");
    assertNotNull(purePerson.getEmail(), "Person should have email");

    assertEquals("Jane", purePerson.getName().getFirstName());
    assertEquals("Doe", purePerson.getName().getLastName());
    assertEquals("0000-0001-2345-6789", purePerson.getOrcid());
    assertEquals("jane.doe@example.com", purePerson.getEmail());
  }

  private String createMockPersonsResponse() {
    try {
      return new String(
          getClass()
              .getClassLoader()
              .getResourceAsStream("org/damap/base/integration/pure/persons.json")
              .readAllBytes());
    } catch (Exception e) {
      throw new RuntimeException("Failed to load persons.json test data", e);
    }
  }

  private String createMockSinglePersonResponse() {
    return """
        {
          "uuid": "a8864c16-5264-4d94-955d-8be7e00f26a9",
          "name": {
            "firstName": "Jane",
            "lastName": "Doe"
          },
          "orcid": "0000-0001-2345-6789",
          "email": "jane.doe@example.com"
        }""";
  }
}
