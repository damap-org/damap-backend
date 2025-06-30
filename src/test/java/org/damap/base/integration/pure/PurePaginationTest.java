package org.damap.base.integration.pure;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** This test makes sure that the pagination works correctly. */
public class PurePaginationTest {

  @Test
  public void testPaginationWorksCorrectly() {
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
    List<PureAPIProject> allProjects = api.listAllProjects();
    Assertions.assertEquals(25, allProjects.size());
  }
}
