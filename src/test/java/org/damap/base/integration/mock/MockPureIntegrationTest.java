package org.damap.base.integration.mock;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.damap.base.integration.pure.PureAPI;
import org.damap.base.integration.pure.PureAPIPerson;
import org.damap.base.integration.pure.PureAPIProject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** PURE integration test using mock data. */
@QuarkusTest
@TestProfile(MockPureTestProfile.class)
public class MockPureIntegrationTest {

  @Inject PureAPI pureAPI;

  @ConfigProperty(name = "damap.elsevier-pure-api-key")
  String apiKey;

  @ConfigProperty(name = "damap.elsevier-pure-endpoint-url")
  String endpointUrl;

  // Mock test UUIDs - these match the data in MockPureAPI
  private static final String[] TEST_PROJECT_UUIDS = {"mock-uuid-1", "mock-uuid-2", "mock-uuid-3"};

  private static final String[] TEST_PERSON_UUIDS = {
    "person-uuid-1", "person-uuid-2", "person-uuid-3"
  };

  @Test
  public void testAPIConnectionConfiguration() {
    System.out.println("Mock PURE API Connection Test");
    System.out.println("Endpoint: " + endpointUrl);
    System.out.println("API Key: " + apiKey.substring(0, 8) + "...");
    Assertions.assertNotNull(pureAPI, "PureAPI should be injected");
    Assertions.assertNotNull(apiKey, "API key should be configured");
    Assertions.assertEquals("mock-api-key", apiKey, "Should use mock API key");
    Assertions.assertTrue(endpointUrl.contains("localhost"), "Should use mock endpoint");
    System.out.println("Mock configuration validation passed");
  }

  @Test
  public void testProjectRetrieval() {
    System.out.println("Testing Project Retrieval with Mock Data");
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

        System.out.printf("  Title: %s\n", project.getTitle().get("en"));
        System.out.printf("  UUID: %s\n", project.getUuid());

      } catch (Exception e) {
        System.out.printf("FAILED - %s: %s\n", e.getClass().getSimpleName(), e.getMessage());
      }
    }

    System.out.printf(
        "Successfully retrieved: %d/%d projects (%.1f%%)\n",
        successCount, totalProjects, (successCount * 100.0 / totalProjects));
    Assertions.assertEquals(totalProjects, successCount, "All mock projects should be accessible");
  }

  @Test
  public void testPersonsFromMockData() {
    System.out.println("Testing Persons Extraction from Mock Data");

    for (String uuid : TEST_PERSON_UUIDS) {
      try {
        System.out.printf("Testing person UUID: %s... ", uuid);
        PureAPIPerson person = pureAPI.getPerson(uuid);
        Assertions.assertNotNull(person, "Person should not be null");
        Assertions.assertEquals(uuid, person.getUuid(), "UUID should match");
        System.out.println("SUCCESS");

        System.out.printf(
            "  Name: %s %s\n", person.getName().getFirstName(), person.getName().getLastName());
        System.out.printf("  Email: %s\n", person.getEmail());
        System.out.printf("  ORCID: %s\n", person.getOrcid());

      } catch (Exception e) {
        System.out.printf("FAILED - %s: %s\n", e.getClass().getSimpleName(), e.getMessage());
        throw e;
      }
    }
    System.out.println("Persons extraction from mock data successful");
  }

  @Test
  public void testPaginationBasic() {
    System.out.println("Testing Pagination with Mock Data");
    try {
      Long offset = 0L;
      Long size = 2L;
      var result = pureAPI.listAllProjects(size, offset);
      Assertions.assertNotNull(result, "Pagination result should not be null");
      System.out.printf(
          "Retrieved %d projects with offset=%d, size=%d\n", result.getCount(), offset, size);

      if (result.getItems() != null) {
        for (PureAPIProject project : result.getItems()) {
          System.out.printf("  - %s (UUID: %s)\n", project.getTitle().get("en"), project.getUuid());
        }
      }

      Long offset2 = 2L;
      var result2 = pureAPI.listAllProjects(size, offset2);
      System.out.printf(
          "Retrieved %d projects with offset=%d, size=%d\n", result2.getCount(), offset2, size);

      System.out.println("Pagination test with mock data successful");
    } catch (Exception e) {
      System.out.printf("FAILED - %s: %s\n", e.getClass().getSimpleName(), e.getMessage());
      throw e;
    }
  }

  @Test
  public void testMockDataConsistency() {
    System.out.println("Testing Mock Data Consistency");

    PureAPIProject project1 = pureAPI.getProject("mock-uuid-1");
    PureAPIProject project2 = pureAPI.getProject("mock-uuid-1");

    Assertions.assertEquals(
        project1.getUuid(), project2.getUuid(), "Repeated calls should return same data");
    Assertions.assertEquals(
        project1.getTitle().get("en"),
        project2.getTitle().get("en"),
        "Titles should be consistent");

    PureAPIProject nonExistent = pureAPI.getProject("non-existent-uuid");
    Assertions.assertNull(nonExistent, "Non-existent project should return null");

    PureAPIPerson nonExistentPerson = pureAPI.getPerson("non-existent-person");
    Assertions.assertNull(nonExistentPerson, "Non-existent person should return null");

    System.out.println("Mock data consistency test passed");
  }
}
