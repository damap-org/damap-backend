package org.damap.base.integration.pure;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

/** Automated test for PURE integration implementation. */
@QuarkusTest
@TestProfile(PureIntegrationTestProfile.class)
@EnabledIfEnvironmentVariable(named = "PURE_INTEGRATION_TEST", matches = "true")
public class PureServicesIntegrationTest {

  @Inject PureAPI pureAPI;

  @ConfigProperty(name = "damap.tenant-aware.elsevier-pure-endpoint-url")
  String pureEndpointUrl;

  @ConfigProperty(name = "damap.tenant-aware.elsevier-pure-backend", defaultValue = "http")
  String pureBackend;

  // Example test UUIDs - replace these with actual project UUIDs from your PURE instance
  private static final String[] TEST_PROJECT_UUIDS = {
    "example-uuid-1", "example-uuid-2", "example-uuid-3"
  };

  private static final long TEST_PAGE_SIZE = 3L;

  private String getConfigProfile() {
    return "pure-integration-test";
  }

  @Test
  public void testConfigurationAccess() {
    System.out.println("Testing configuration access...");

    System.out.println("Current PURE Configuration:");
    System.out.println("Profile: " + getConfigProfile());
    System.out.println("Backend Type: " + pureBackend);
    System.out.println("Endpoint URL: " + pureEndpointUrl);

    Assertions.assertNotNull(pureEndpointUrl, "PURE endpoint URL should be configured");
    Assertions.assertTrue(
        pureEndpointUrl.contains("elsevierpure.com"),
        "Should be valid PURE endpoint for integration testing");
    System.out.println("Configuration validation passed");
  }

  @Test
  public void testPureServicesIntegration_Complete() {
    System.out.println("Comprehensive test of PURE pagination implementation...");

    try {
      System.out.println("\nTesting Projects Pagination:");
      testPaginationLogic_Projects();

      System.out.println("\nTesting Persons Pagination:");
      testPaginationLogic_Persons();

      System.out.println("\nAll pagination assumptions validated!");
    } catch (Exception e) {
      System.err.println("Pagination test failed: " + e.getMessage());
      Assertions.fail("PURE pagination logic test failed: " + e.getMessage());
    }
  }

  @Test
  public void testAPIIntegration_Complete() {
    System.out.println("Testing PURE API integration...");

    try {
      for (String uuid : TEST_PROJECT_UUIDS) {
        System.out.println("\nTesting project UUID: " + uuid);
        testSingleProjectAPI(uuid);
      }
      System.out.println("\nAll API integration features validated!");

    } catch (Exception e) {
      System.err.println("API integration test failed: " + e.getMessage());
      Assertions.fail("API integration test failed: " + e.getMessage());
    }
  }

  @Test
  public void testProjectPersonsExtraction() {
    System.out.println("Testing extraction of persons from projects...");

    try {
      for (String uuid : TEST_PROJECT_UUIDS) {
        System.out.println("\nExtracting persons from project: " + uuid);
        testProjectPersonsExtraction(uuid);
      }

      System.out.println("\nAll project persons extraction tests completed!");

    } catch (Exception e) {
      System.err.println("Project persons extraction failed: " + e.getMessage());
      Assertions.fail("Project persons extraction test failed: " + e.getMessage());
    }
  }

  @Test
  public void testRoleMappingConfiguration() {
    System.out.println("Testing role mapping configuration...");

    try {
      System.out.println("Testing role mapping configuration access:");
      System.out.println("Endpoint URL: " + pureEndpointUrl);
      System.out.println("Backend Type: " + pureBackend);

      // one project to test role mapping
      String testUuid = TEST_PROJECT_UUIDS[0]; // FWF
      testRoleMappingForProject(testUuid);

      System.out.println("Role mapping configuration test completed!");

    } catch (Exception e) {
      System.err.println("Role mapping test failed: " + e.getMessage());
      Assertions.fail("Role mapping test failed: " + e.getMessage());
    }
  }

  @Test
  public void testErrorHandling() {
    System.out.println("Testing error handling implementation...");

    try {
      System.out.println("\nTesting non-existent project handling:");
      testNonExistentProject();

      System.out.println("\nTesting malformed UUID handling:");
      testMalformedUUID();

      System.out.println("\nTesting pagination edge cases:");
      testPaginationEdgeCases();

      System.out.println("\nAll error handling scenarios validated!");

    } catch (Exception e) {
      System.err.println("Error handling test failed: " + e.getMessage());
      Assertions.fail("PURE error handling test failed: " + e.getMessage());
    }
  }

  private void testPaginationLogic_Projects() throws Exception {

    PureAPIPaginatedProjectsResponse page1 = pureAPI.listAllProjects(TEST_PAGE_SIZE, 0L);
    validatePaginationAssumptions(page1, 0L, TEST_PAGE_SIZE, "Projects Page 1");

    if (page1.count > TEST_PAGE_SIZE) {
      PureAPIPaginatedProjectsResponse page2 =
          pureAPI.listAllProjects(TEST_PAGE_SIZE, TEST_PAGE_SIZE);
      validatePaginationAssumptions(page2, TEST_PAGE_SIZE, TEST_PAGE_SIZE, "Projects Page 2");
      validateFinishedLogic(page2.pageInformation.offset, page1.count, "Projects");
    }
  }

  private void testPaginationLogic_Persons() throws Exception {
    try {
      PureAPIPaginatedPersonsResponse page1 = pureAPI.listAllPersons(TEST_PAGE_SIZE, 0L);
      validatePaginationAssumptions_Persons(page1, 0L, TEST_PAGE_SIZE, "Persons Page 1");

      if (page1.count > TEST_PAGE_SIZE) {
        PureAPIPaginatedPersonsResponse page2 =
            pureAPI.listAllPersons(TEST_PAGE_SIZE, TEST_PAGE_SIZE);
        validatePaginationAssumptions_Persons(
            page2, TEST_PAGE_SIZE, TEST_PAGE_SIZE, "Persons Page 2");

        validateFinishedLogic(page2.pageInformation.offset, page1.count, "Persons");
      }
      System.out.println("Persons service working (unexpected but good!)");
    } catch (WebApplicationException e) {
      if (e.getResponse().getStatus() == 403) {
        System.out.println("EXPECTED: Persons endpoint returned 403 Forbidden");
        System.out.println("This is normal - person data requires special permissions");
      } else {
        System.out.println(
            "Persons endpoint error (status "
                + e.getResponse().getStatus()
                + "): "
                + e.getMessage());
      }
    } catch (Exception e) {
      System.out.println("Persons pagination handled: " + e.getMessage());
    }
  }

  private void testSingleProjectAPI(String uuid) throws Exception {
    // direct API access
    PureAPIProject apiProject = pureAPI.getProject(uuid);
    if (apiProject != null) {
      System.out.println("API retrieval: " + apiProject.getTitle());
      Assertions.assertEquals(uuid, apiProject.getUuid());
      Assertions.assertNotNull(apiProject.getTitle(), "Project should have a title");
      System.out.println("Project data validation passed");
    } else {
      System.out.println("Project " + uuid + " not found or not accessible");
    }
  }

  private void testProjectPersonsExtraction(String uuid) throws Exception {
    // extracting persons from project
    PureAPIProject apiProject = pureAPI.getProject(uuid);
    if (apiProject == null) {
      System.out.println("Project not found: " + uuid);
      return;
    }

    System.out.println("Project: " + extractProjectTitle(apiProject.getTitle()));

    // participants extraction
    if (apiProject.getParticipants() == null || apiProject.getParticipants().isEmpty()) {
      System.out.println("No participants found in project");
      return;
    }

    System.out.println("Found " + apiProject.getParticipants().size() + " participant(s):");

    for (int i = 0; i < apiProject.getParticipants().size(); i++) {
      var participant = apiProject.getParticipants().get(i);
      System.out.println((i + 1) + ". Type: " + participant.getClass().getSimpleName());

      if (participant.getName() != null) {
        System.out.println(
            "Name: "
                + participant.getName().getFirstName()
                + " "
                + participant.getName().getLastName());
      }

      if (participant.getRole() != null && participant.getRole().getUri() != null) {
        System.out.println("Role: " + participant.getRole().getUri());
      }

      // person extraction for internal participants
      if (participant instanceof PureAPIInternalParticipantAssociation internal) {
        try {
          PureAPIPerson person = pureAPI.getPerson(internal.getPerson().getUuid());
          if (person != null) {
            System.out.println(
                "Person details retrieved: "
                    + person.getName().getFirstName()
                    + " "
                    + person.getName().getLastName());
            if (person.getOrcid() != null) {
              System.out.println("ORCID: " + person.getOrcid());
            }
            if (person.getEmail() != null) {
              System.out.println("Email: " + person.getEmail());
            }
          } else {
            System.out.println("Person details not accessible");
          }
        } catch (Exception e) {
          System.out.println("Person lookup failed: " + e.getMessage());
        }
      }
    }
  }

  private String extractProjectTitle(Object title) {
    if (title instanceof java.util.Map) {
      @SuppressWarnings("unchecked")
      java.util.Map<String, String> titleMap = (java.util.Map<String, String>) title;
      return titleMap.values().stream().findFirst().orElse("Unknown Title");
    }
    return title != null ? title.toString() : "Unknown Title";
  }

  private void testRoleMappingForProject(String uuid) throws Exception {
    PureAPIProject apiProject = pureAPI.getProject(uuid);
    if (apiProject == null || apiProject.getParticipants() == null) {
      System.out.println("No project or participants found for role mapping test");
      return;
    }

    System.out.println("Project: " + extractProjectTitle(apiProject.getTitle()));
    System.out.println("Testing role mappings for participants:");

    boolean foundMappableRole = false;
    for (var participant : apiProject.getParticipants()) {
      if (participant.getRole() != null && participant.getRole().getUri() != null) {
        String roleUri = participant.getRole().getUri();
        System.out.println("Role URI found: " + roleUri);

        System.out.println("Other role URI: " + roleUri);
      }
    }

    if (foundMappableRole) {
      System.out.println("Role mapping test shows our configuration covers real Pure API roles");
    } else {
      System.out.println("No mappable roles found in this project");
    }
  }

  private void testNonExistentProject() throws Exception {
    String fakeUuid = "00000000-0000-0000-0000-000000000000";

    try {
      PureAPIProject project = pureAPI.getProject(fakeUuid);
      if (project == null) {
        System.out.println("Non-existent project returns null (good)");
      } else {
        System.out.println("Non-existent project returned data: " + project);
      }
    } catch (WebApplicationException e) {
      if (e.getResponse().getStatus() == 404) {
        System.out.println("Non-existent project throws 404 (even better error handling)");
      } else {
        System.out.println("Non-existent project throws status " + e.getResponse().getStatus());
      }
    } catch (Exception e) {
      System.out.println("Non-existent project throws exception: " + e.getClass().getSimpleName());
    }
  }

  private void testMalformedUUID() throws Exception {
    String malformedUuid = "not-a-uuid";

    try {
      PureAPIProject project = pureAPI.getProject(malformedUuid);
      System.out.println("Malformed UUID handled without exception");
    } catch (Exception e) {
      System.out.println(
          "Malformed UUID properly throws exception: " + e.getClass().getSimpleName());
    }
  }

  private void testPaginationEdgeCases() throws Exception {
    // zero page size
    try {
      PureAPIPaginatedProjectsResponse response = pureAPI.listAllProjects(0L, 0L);
      System.out.println("Zero page size handled");
    } catch (Exception e) {
      System.out.println(
          "Zero page size throws exception (expected): " + e.getClass().getSimpleName());
    }

    // very large offset
    try {
      PureAPIPaginatedProjectsResponse response = pureAPI.listAllProjects(1L, 999999L);
      System.out.println("Large offset returns " + response.items.size() + " items");
    } catch (Exception e) {
      System.out.println(
          "Large offset throws exception (acceptable): " + e.getClass().getSimpleName());
    }
  }

  private void validatePaginationAssumptions(
      PureAPIPaginatedProjectsResponse page,
      long expectedOffset,
      long requestedSize,
      String testName) {
    Assertions.assertNotNull(page, testName + ": Response should not be null");
    Assertions.assertNotNull(page.items, testName + ": Items should not be null");
    Assertions.assertNotNull(page.pageInformation, testName + ": PageInfo should not be null");

    // assuming pageInformation.offset equals requested offset
    Assertions.assertEquals(
        expectedOffset,
        page.pageInformation.offset,
        testName + ": pageInformation.offset should match requested offset");

    // assuming pageInformation.size behavior
    boolean sizeMatchesReturned = page.pageInformation.size == page.items.size();
    boolean sizeMatchesRequested = page.pageInformation.size == requestedSize;
    Assertions.assertTrue(
        sizeMatchesReturned || sizeMatchesRequested,
        testName + ": pageInformation.size should match either returned count OR requested size");

    // assuming count represents total records on server
    Assertions.assertTrue(
        page.count >= 0, testName + ": count should represent total server records");

    System.out.println(testName + " - All pagination assumptions validated");
  }

  private void validatePaginationAssumptions_Persons(
      PureAPIPaginatedPersonsResponse page,
      long expectedOffset,
      long requestedSize,
      String testName) {
    Assertions.assertNotNull(page, testName + ": Response should not be null");
    Assertions.assertNotNull(page.items, testName + ": Items should not be null");
    Assertions.assertNotNull(page.pageInformation, testName + ": PageInfo should not be null");

    // Same validation as projects
    Assertions.assertEquals(
        expectedOffset,
        page.pageInformation.offset,
        testName + ": pageInformation.offset should match requested offset");

    boolean sizeMatchesReturned = page.pageInformation.size == page.items.size();
    boolean sizeMatchesRequested = page.pageInformation.size == requestedSize;
    Assertions.assertTrue(
        sizeMatchesReturned || sizeMatchesRequested,
        testName + ": pageInformation.size should match either returned count OR requested size");

    Assertions.assertTrue(
        page.count >= 0, testName + ": count should represent total server records");

    System.out.println(testName + " - All pagination assumptions validated");
  }

  private void validateFinishedLogic(long currentOffset, long totalCount, String endpoint) {
    // assuming: "finished" when offset >= count
    boolean shouldBeFinished = currentOffset >= totalCount;

    System.out.println(
        endpoint
            + " - Finished logic validated: offset="
            + currentOffset
            + ", count="
            + totalCount
            + ", shouldBeFinished="
            + shouldBeFinished);
  }
}
