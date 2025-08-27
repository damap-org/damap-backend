package org.damap.base.integration.pure;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@QuarkusTest
@TestProfile(PureIntegrationTestProfile.class)
@EnabledIfEnvironmentVariable(named = "PURE_INTEGRATION_TEST", matches = "true")
public class PurePaginationBehaviorTest {

  @Inject PureAPI pureAPI;

  @Test
  public void testProjectsPaginationBehavior() {
    System.out.println("Testing PROJECTS Pagination Behavior");
    System.out.println("====================================");
    testProjectsPagination(0L, 5L);
    testProjectsPagination(5L, 5L);
    testProjectsPagination(10L, 3L);
    testProjectsPagination(100L, 10L);
    System.out.println("Projects pagination analysis complete");
  }

  @Test
  public void testPersonsPaginationBehavior() {
    System.out.println("Testing PERSONS Pagination Behavior");
    System.out.println("===================================");
    try {
      testPersonsPagination(0L, 5L);
      testPersonsPagination(5L, 5L);
    } catch (Exception e) {
      System.out.println("Persons API returned error (expected): " + e.getMessage());
      System.out.println("This is normal - person data requires special permissions");
    }
    System.out.println("Persons pagination analysis complete");
  }

  private void testProjectsPagination(Long inputOffset, Long inputSize) {
    System.out.printf("Testing Projects: offset=%d, size=%d\n", inputOffset, inputSize);
    try {
      PureAPIPaginatedProjectsResponse response = pureAPI.listAllProjects(inputSize, inputOffset);
      System.out.printf("Input Offset: %d\n", inputOffset);
      System.out.printf("Input Size: %d\n", inputSize);
      System.out.printf("Total Count: %d\n", response.getCount());
      System.out.printf(
          "Actual Items Returned: %d\n",
          response.getItems() != null ? response.getItems().size() : 0);
      if (response.getPageInformation() != null) {
        System.out.printf(
            "pageInformation.offset: %d\n", response.getPageInformation().getOffset());
        System.out.printf("pageInformation.size: %d\n", response.getPageInformation().getSize());
        boolean offsetEchoed = response.getPageInformation().getOffset() == inputOffset;
        boolean sizeEchoed = response.getPageInformation().getSize() == inputSize;
        boolean sizeIsActualCount =
            response.getItems() != null
                && response.getPageInformation().getSize() == response.getItems().size();
        System.out.printf("Offset echoed back input? %s\n", offsetEchoed ? "YES" : "NO");
        System.out.printf("Size echoed back input? %s\n", sizeEchoed ? "YES" : "NO");
        System.out.printf(
            "Size equals actual items returned? %s\n", sizeIsActualCount ? "YES" : "NO");
        long nextOffset =
            response.getPageInformation().getOffset() + response.getPageInformation().getSize();
        boolean wouldTerminate = response.getCount() <= nextOffset;
        System.out.printf(
            "Would pagination terminate? %s (count=%d, nextOffset=%d)\n",
            wouldTerminate ? "YES" : "NO", response.getCount(), nextOffset);
      } else {
        System.out.println("No pageInformation in response");
      }
    } catch (Exception e) {
      System.out.printf("Error: %s\n", e.getMessage());
    }
  }

  private void testPersonsPagination(Long inputOffset, Long inputSize) {
    System.out.printf("Testing Persons: offset=%d, size=%d\n", inputOffset, inputSize);
    try {
      PureAPIPaginatedPersonsResponse response = pureAPI.listAllPersons(inputSize, inputOffset);
      System.out.printf("Input Offset: %d\n", inputOffset);
      System.out.printf("Input Size: %d\n", inputSize);
      System.out.printf("Total Count: %d\n", response.getCount());
      System.out.printf(
          "Actual Items Returned: %d\n",
          response.getItems() != null ? response.getItems().size() : 0);
      if (response.getPageInformation() != null) {
        System.out.printf(
            "pageInformation.offset: %d\n", response.getPageInformation().getOffset());
        System.out.printf("pageInformation.size: %d\n", response.getPageInformation().getSize());
        boolean offsetEchoed = response.getPageInformation().getOffset() == inputOffset;
        boolean sizeEchoed = response.getPageInformation().getSize() == inputSize;
        boolean sizeIsActualCount =
            response.getItems() != null
                && response.getPageInformation().getSize() == response.getItems().size();
        System.out.printf("Offset echoed back input? %s\n", offsetEchoed ? "YES" : "NO");
        System.out.printf("Size echoed back input? %s\n", sizeEchoed ? "YES" : "NO");
        System.out.printf(
            "Size equals actual items returned? %s\n", sizeIsActualCount ? "YES" : "NO");
      }
    } catch (Exception e) {
      System.out.printf("Error: %s\n", e.getMessage());
      throw e;
    }
  }

  @Test
  public void testPaginationLogicCorrectness() {
    System.out.println("Testing Current Pagination Logic");
    System.out.println("==============================");
    long offset = 0;
    long pageSize = 10;
    int pageCount = 0;
    int totalItemsCollected = 0;
    try {
      while (pageCount < 5) {
        pageCount++;
        System.out.printf("Page %d\n", pageCount);
        System.out.printf("Requesting: offset=%d, size=%d\n", offset, pageSize);
        PureAPIPaginatedProjectsResponse response = pureAPI.listAllProjects(pageSize, offset);
        int itemsThisPage = response.getItems() != null ? response.getItems().size() : 0;
        totalItemsCollected += itemsThisPage;
        System.out.printf("Response: count=%d, items=%d\n", response.getCount(), itemsThisPage);
        System.out.printf(
            "PageInfo: offset=%d, size=%d\n",
            response.getPageInformation().getOffset(), response.getPageInformation().getSize());
        offset =
            response.getPageInformation().getOffset() + response.getPageInformation().getSize();
        boolean finished = response.getCount() <= offset;
        System.out.printf("Next offset would be: %d\n", offset);
        System.out.printf(
            "Would finish? %s (count=%d <= offset=%d)\n", finished, response.getCount(), offset);
        System.out.printf("Total items collected so far: %d\n", totalItemsCollected);
        if (finished) {
          System.out.println("Pagination would terminate here");
          break;
        }
      }
    } catch (Exception e) {
      System.out.printf("Error during pagination simulation: %s\n", e.getMessage());
    }
    System.out.println("Summary:");
    System.out.printf("Processed %d pages\n", pageCount);
    System.out.printf("Collected %d total items\n", totalItemsCollected);
    System.out.println("This shows if the current pagination logic works correctly");
  }
}
