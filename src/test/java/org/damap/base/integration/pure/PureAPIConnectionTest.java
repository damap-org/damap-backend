package org.damap.base.integration.pure;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@QuarkusTest
@TestProfile(PureIntegrationTestProfile.class)
@EnabledIfEnvironmentVariable(named = "PURE_INTEGRATION_TEST", matches = "true")
public class PureAPIConnectionTest {

  @Inject PureAPI pureAPI;

  @ConfigProperty(name = "damap.elsevier-pure-api-key")
  String apiKey;

  @ConfigProperty(name = "damap.elsevier-pure-endpoint-url")
  String endpointUrl;

  private static final String[] TEST_PROJECT_UUIDS = {
    "18fb01c4-5801-468b-8818-43bde001f43d",
    "0cd8eb67-fa83-469b-9398-4563041305d9",
    "74b78a48-9498-45c1-87ff-63be5e5410e9",
    "622713ab-7fa0-4c2b-b916-6e36779f120e",
    "b9fa4f5f-f6f1-4d24-aa61-1ca3e5d39bfe",
    "06eb8491-9125-4cc8-88bf-981cf9a34f1a",
    "d43d4520-daa9-4329-a508-d7372d04ad4f",
    "03e4e414-570b-4447-b187-19a0468e385c"
  };

  @Test
  public void testAPIConnectionConfiguration() {
    System.out.println("PURE API Connection Test");
    System.out.println("Endpoint: " + endpointUrl);
    System.out.println("API Key: " + apiKey.substring(0, 8) + "...");
    Assertions.assertNotNull(pureAPI, "PureAPI should be injected");
    Assertions.assertNotNull(apiKey, "API key should be configured");
    Assertions.assertTrue(apiKey.startsWith("b62c34ab"), "Should use new API key");
    Assertions.assertTrue(
        endpointUrl.contains("tugraz-staging.elsevierpure.com"), "Should use correct endpoint");
    System.out.println("Configuration validation passed");
  }

  @Test
  public void testProjectRetrieval() {
    System.out.println("Testing Project Retrieval");
    int successCount = 0;
    int totalProjects = TEST_PROJECT_UUIDS.length;
    for (String uuid : TEST_PROJECT_UUIDS) {
      try {
        System.out.printf("Testing project UUID: %s... ", uuid);
        PureAPIProject project = pureAPI.getProject(uuid);
        Assertions.assertNotNull(project, "Project should not be null");
        Assertions.assertEquals(uuid, project.getUuid(), "UUID should match");
        System.out.println("SUCCESS");
        successCount++;
        System.out.printf("  Title: %s\n", project.getTitle().get("en_GB"));
        System.out.printf(
            "  Participants: %d\n",
            project.getParticipants() != null ? project.getParticipants().size() : 0);
      } catch (WebApplicationException e) {
        System.out.printf("FAILED - HTTP %d: %s\n", e.getResponse().getStatus(), e.getMessage());
        if (e.getResponse().getStatus() == 401) {
          Assertions.fail("Authentication failed - API key may be invalid or expired");
        } else if (e.getResponse().getStatus() == 404) {
          System.out.println("  Project not found (expected for some test UUIDs)");
        } else {
          System.out.printf("  Unexpected error: %s\n", e.getMessage());
        }
      } catch (Exception e) {
        System.out.printf("FAILED - %s: %s\n", e.getClass().getSimpleName(), e.getMessage());
      }
    }
    System.out.printf(
        "Successfully retrieved: %d/%d projects (%.1f%%)\n",
        successCount, totalProjects, (successCount * 100.0 / totalProjects));
    Assertions.assertTrue(
        successCount > 0, "At least some test projects should be accessible with valid API key");
  }

  @Test
  public void testPersonsFromProject() {
    System.out.println("Testing Persons Extraction");
    String testUuid = TEST_PROJECT_UUIDS[0];
    try {
      PureAPIProject project = pureAPI.getProject(testUuid);
      Assertions.assertNotNull(project, "Test project should be accessible");
      var participants = project.getParticipants();
      System.out.printf(
          "Project '%s' has %d participants\n",
          project.getTitle().get("en_GB"), participants != null ? participants.size() : 0);
      if (participants != null && !participants.isEmpty()) {
        for (var participant : participants) {
          System.out.printf(
              "  - %s %s (Role: %s)\n",
              participant.getName().getFirstName(),
              participant.getName().getLastName(),
              participant.getRole().getTerm().get("en_GB"));
        }
        System.out.println("Persons extraction successful");
      } else {
        System.out.println("No participants found in test project");
      }
    } catch (Exception e) {
      System.out.printf("FAILED - %s: %s\n", e.getClass().getSimpleName(), e.getMessage());
      throw e;
    }
  }

  @Test
  public void testPaginationBasic() {
    System.out.println("Testing Pagination");
    try {
      Long offset = 0L;
      Long size = 3L;
      var result = pureAPI.listAllProjects(offset, size);
      Assertions.assertNotNull(result, "Pagination result should not be null");
      System.out.printf(
          "Retrieved %d projects with offset=%d, size=%d\n", result.getCount(), offset, size);
      if (result.getItems() != null) {
        for (PureAPIProject project : result.getItems()) {
          System.out.printf(
              "  - %s (UUID: %s)\n", project.getTitle().get("en_GB"), project.getUuid());
        }
      }
      System.out.println("Pagination test successful");
    } catch (Exception e) {
      System.out.printf("FAILED - %s: %s\n", e.getClass().getSimpleName(), e.getMessage());
      throw e;
    }
  }
}
