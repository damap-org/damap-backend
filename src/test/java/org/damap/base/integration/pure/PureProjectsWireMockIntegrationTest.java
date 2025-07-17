package org.damap.base.integration.pure;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** WireMock-based Pure Projects integration tests. */
@QuarkusTest
@TestProfile(WireMockPureTestProfile.class)
public class PureProjectsWireMockIntegrationTest {

  @Inject PureAPI pureAPI;

  @ConfigProperty(name = "damap.elsevier-pure-api-key")
  String apiKey;

  private WireMockServer wireMockServer;
  private String wireMockUrl;

  @BeforeEach
  public void setup() {
    // using WireMock server on fixed port 8089 to match test profile configuration
    wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
    wireMockServer.start();
    wireMockUrl = "http://localhost:8089";

    System.out.println("WireMock server started at: " + wireMockUrl);
  }

  @AfterEach
  public void teardown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  public void testHTTPProjectsEndpoint() {
    // using real JSON test data
    wireMockServer.stubFor(
        get(urlPathEqualTo("/projects"))
            .withHeader("api-key", equalTo("test-api-key"))
            .withQueryParam("size", equalTo("10"))
            .withQueryParam("offset", equalTo("0"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createMockProjectsResponse())));

    PureAPIPaginatedProjectsResponse response = pureAPI.listAllProjects(10L, 0L);

    assertNotNull(response, "Response should not be null");
    assertNotNull(response.getItems(), "Items should not be null");
    assertEquals(2, response.getItems().size(), "Should have 2 projects from test data");

    PureAPIProject firstProject = response.getItems().get(0);
    assertEquals("430daa1b-f722-4649-b638-b11e056dbc85", firstProject.getUuid());
    assertEquals("Test  1", firstProject.getTitle().get("en_GB"));

    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/projects"))
            .withHeader("api-key", equalTo("test-api-key"))
            .withQueryParam("size", equalTo("10"))
            .withQueryParam("offset", equalTo("0")));
  }

  @Test
  public void testHTTPSingleProjectEndpoint() {
    String projectUuid = "430daa1b-f722-4649-b638-b11e056dbc85";

    wireMockServer.stubFor(
        get(urlPathEqualTo("/projects/" + projectUuid))
            .withHeader("api-key", equalTo("test-api-key"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createMockSingleProjectResponse())));

    PureAPIProject project = pureAPI.getProject(projectUuid);

    assertNotNull(project, "Project should not be null");
    assertEquals(projectUuid, project.getUuid());
    assertEquals("test1", project.getAcronym());
    assertEquals("Test  1", project.getTitle().get("en_GB"));

    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/projects/" + projectUuid))
            .withHeader("api-key", equalTo("test-api-key")));
  }

  @Test
  public void testProjectDataValidation() {
    String projectUuid = "430daa1b-f722-4649-b638-b11e056dbc85";

    // single project endpoint
    wireMockServer.stubFor(
        get(urlPathEqualTo("/projects/" + projectUuid))
            .withHeader("api-key", equalTo("test-api-key"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(createMockSingleProjectResponse())));

    PureAPIProject pureProject = pureAPI.getProject(projectUuid);

    assertNotNull(pureProject, "Pure API should return project");

    System.out.println("=== PURE PROJECTS API DATA ===");
    System.out.println("Project UUID: " + pureProject.getUuid());
    System.out.println("Project Title (en_GB): " + pureProject.getTitle().get("en_GB"));
    System.out.println("Project Title (de_DE): " + pureProject.getTitle().get("de_DE"));
    System.out.println("Project Acronym: " + pureProject.getAcronym());
    System.out.println("Project Start: " + pureProject.getPeriod().getStartDate());
    System.out.println("Project End: " + pureProject.getPeriod().getEndDate());
    System.out.println("Participants: " + pureProject.getParticipants().size());

    assertNotNull(pureProject.getTitle(), "Project should have a title");
    assertNotNull(pureProject.getAcronym(), "Project should have an acronym");
    assertNotNull(pureProject.getPeriod(), "Project should have a period");
    assertNotNull(pureProject.getParticipants(), "Project should have participants");
    assertNotNull(pureProject.getPeriod().getStartDate(), "Project should have start date");
    assertNotNull(pureProject.getPeriod().getEndDate(), "Project should have end date");

    assertEquals("Test  1", pureProject.getTitle().get("en_GB"));
    assertEquals("Test Eins", pureProject.getTitle().get("de_DE"));
    assertEquals("test1", pureProject.getAcronym());
    assertEquals(1, pureProject.getParticipants().size(), "Should have 1 participant");

    var firstParticipant = pureProject.getParticipants().get(0);
    assertNotNull(firstParticipant, "Should have a participant");
    assertNotNull(firstParticipant.getName(), "Participant should have a name");
    assertNotNull(firstParticipant.getRole(), "Participant should have a role");

    System.out.println(
        "Participant Name: "
            + firstParticipant.getName().getFirstName()
            + " "
            + firstParticipant.getName().getLastName());
    System.out.println("Participant Role URI: " + firstParticipant.getRole().getUri());

    if (firstParticipant instanceof PureAPIInternalParticipantAssociation internal) {
      String participantPersonUuid = internal.getPerson().getUuid();
      System.out.println("Participant Person UUID: " + participantPersonUuid);
      assertEquals(
          "a8864c16-5264-4d94-955d-8be7e00f26a9",
          participantPersonUuid,
          "Participant should reference expected person UUID");
    }
  }

  @Test
  public void testProjectsAPIKeyAuthenticationFailure() {
    wireMockServer.stubFor(
        get(urlPathEqualTo("/projects"))
            .willReturn(
                aResponse()
                    .withStatus(401)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"error\": \"Unauthorized\"}")));

    assertThrows(
        Exception.class,
        () -> {
          pureAPI.listAllProjects(10L, 0L);
        },
        "Should throw exception for unauthorized access");

    wireMockServer.verify(
        getRequestedFor(urlPathEqualTo("/projects"))
            .withQueryParam("size", equalTo("10"))
            .withQueryParam("offset", equalTo("0")));
  }

  // using mock JSON responses from existing test files

  private String createMockProjectsResponse() {
    try {
      return new String(
          getClass()
              .getClassLoader()
              .getResourceAsStream("org/damap/base/integration/pure/projects.json")
              .readAllBytes());
    } catch (Exception e) {
      throw new RuntimeException("Failed to load projects.json test data", e);
    }
  }

  private String createMockSingleProjectResponse() {
    return """
        {
          "typeDiscriminator": "AwardManagementProject",
          "uuid": "430daa1b-f722-4649-b638-b11e056dbc85",
          "acronym": "test1",
          "title": {
            "en_GB": "Test  1",
            "de_DE": "Test Eins"
          },
          "period": {
            "startDate": "2021-01-01",
            "endDate": "2026-01-01"
          },
          "participants": [
            {
              "typeDiscriminator": "InternalParticipantAssociation",
              "pureId": 1234567,
              "name": {
                "firstName": "Jane",
                "lastName": "Doe"
              },
              "role": {
                "uri": "/dk/atira/pure/test/projectlead",
                "term": {
                  "de_DE": "Projektleitung"
                }
              },
              "person": {
                "systemName": "Person",
                "uuid": "a8864c16-5264-4d94-955d-8be7e00f26a9"
              }
            }
          ]
        }""";
  }
}
