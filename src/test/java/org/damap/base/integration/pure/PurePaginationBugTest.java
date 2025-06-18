package org.damap.base.integration.pure;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/** Test demonstrating the pagination bug in PureAPI default methods. */
public class PurePaginationBugTest {

  @Test
  @Timeout(value = 5, unit = TimeUnit.SECONDS)
  public void testPaginationBugCausesInfiniteLoop() {
    PureAPI api =
        new PureAPI() {
          private int callCount = 0;

          @Override
          public PureAPIPaginatedProjectsResponse listAllProjects(Long size, Long offset) {
            callCount++;
            System.out.println(
                "API Call #" + callCount + " - offset: " + offset + ", size: " + size);

            // stop after 10 calls to prevent actual infinite loop
            if (callCount > 10) {
              throw new RuntimeException(
                  "Too many API calls - infinite loop detected! Called "
                      + callCount
                      + " times with offset always = "
                      + offset);
            }

            PureAPIPaginatedProjectsResponse response = new PureAPIPaginatedProjectsResponse();
            response.count = 25; // More items than page size (10)
            response.pageInformation = new PureAPIPageInformation();
            response.pageInformation.offset = offset;
            response.pageInformation.size = Math.min(size.intValue(), 10);

            response.items = new ArrayList<>();
            int startIndex = offset.intValue();
            int itemsToReturn = Math.min(10, (int) (25 - offset));

            for (int i = 0; i < itemsToReturn; i++) {
              PureAPIProject project = new PureAPIProject();
              project.uuid = "project-" + String.format("%03d", startIndex + i + 1);
              project.acronym = "P" + String.format("%03d", startIndex + i + 1);
              response.items.add(project);
            }

            System.out.println(
                "Returning "
                    + response.items.size()
                    + " items, pageInfo.offset="
                    + response.pageInformation.offset);
            return response;
          }

          @Override
          public PureAPIProject getProject(String uuid) {
            return null;
          }

          @Override
          public PureAPIPaginatedPersonsResponse listAllPersons(Long size, Long offset) {
            return null;
          }

          @Override
          public PureAPIPerson getPerson(String uuid) {
            return null;
          }
        };

    // should fail due to infinite loop
    Exception exception =
        Assertions.assertThrows(
            RuntimeException.class,
            () -> {
              System.out.println(
                  "Calling api.listAllProjects() - this should cause infinite loop...");
              List<PureAPIProject> allProjects = api.listAllProjects();
              System.out.println(
                  "Got " + allProjects.size() + " projects - this shouldn't be reached!");
            });

    System.out.println("Exception caught: " + exception.getMessage());
    Assertions.assertTrue(exception.getMessage().contains("infinite loop detected"));
    Assertions.assertTrue(exception.getMessage().contains("offset always = 0"));
  }
}
