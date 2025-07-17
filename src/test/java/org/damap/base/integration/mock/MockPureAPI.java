package org.damap.base.integration.mock;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.damap.base.integration.pure.PureAPI;
import org.damap.base.integration.pure.PureAPIName;
import org.damap.base.integration.pure.PureAPIPaginatedPersonsResponse;
import org.damap.base.integration.pure.PureAPIPaginatedProjectsResponse;
import org.damap.base.integration.pure.PureAPIPerson;
import org.damap.base.integration.pure.PureAPIProject;

/**
 * Mock PURE API implementation for testing data that can be used for unit tests and integration
 * tests.
 */
@Mock
@ApplicationScoped
public class MockPureAPI implements PureAPI {

  private static final List<PureAPIProject> MOCK_PROJECTS =
      Arrays.asList(
          createMockProject(
              "mock-uuid-1",
              "Sample Research Project 1",
              "This is a sample research project for testing purposes"),
          createMockProject(
              "mock-uuid-2",
              "Sample Research Project 2",
              "Another sample research project for testing"),
          createMockProject(
              "mock-uuid-3",
              "Sample Research Project 3",
              "Third sample research project for testing"));

  private static final List<PureAPIPerson> MOCK_PERSONS =
      Arrays.asList(
          createMockPerson(
              "person-uuid-1", "John", "Doe", "john.doe@example.com", "0000-0000-0000-0001"),
          createMockPerson(
              "person-uuid-2", "Jane", "Smith", "jane.smith@example.com", "0000-0000-0000-0002"),
          createMockPerson("person-uuid-3", "Bob", "Johnson", "bob.johnson@example.com", null));

  @Override
  public PureAPIPaginatedProjectsResponse listAllProjects(Long size, Long offset) {
    PureAPIPaginatedProjectsResponse response = new PureAPIPaginatedProjectsResponse();

    long startIndex = offset != null ? offset : 0;
    long endIndex =
        size != null ? Math.min(startIndex + size, MOCK_PROJECTS.size()) : MOCK_PROJECTS.size();

    List<PureAPIProject> page = MOCK_PROJECTS.subList((int) startIndex, (int) endIndex);
    response.setItems(new ArrayList<>(page));
    response.setCount((long) page.size());

    return response;
  }

  @Override
  public PureAPIProject getProject(String uuid) {
    return MOCK_PROJECTS.stream()
        .filter(project -> project.getUuid().equals(uuid))
        .findFirst()
        .orElse(null);
  }

  @Override
  public PureAPIPaginatedPersonsResponse listAllPersons(Long size, Long offset) {
    PureAPIPaginatedPersonsResponse response = new PureAPIPaginatedPersonsResponse();

    long startIndex = offset != null ? offset : 0;
    long endIndex =
        size != null ? Math.min(startIndex + size, MOCK_PERSONS.size()) : MOCK_PERSONS.size();

    List<PureAPIPerson> page = MOCK_PERSONS.subList((int) startIndex, (int) endIndex);
    response.setItems(new ArrayList<>(page));
    response.setCount((long) page.size());

    return response;
  }

  @Override
  public PureAPIPerson getPerson(String uuid) {
    return MOCK_PERSONS.stream()
        .filter(person -> person.getUuid().equals(uuid))
        .findFirst()
        .orElse(null);
  }

  private static PureAPIProject createMockProject(String uuid, String title, String description) {
    PureAPIProject project = new PureAPIProject();
    project.setUuid(uuid);

    HashMap<String, String> titleMap = new HashMap<>();
    titleMap.put("en", title);
    project.setTitle(titleMap);

    // optional: expand project: add project data
    return project;
  }

  private static PureAPIPerson createMockPerson(
      String uuid, String firstName, String lastName, String email, String orcid) {
    PureAPIPerson person = new PureAPIPerson();
    person.setUuid(uuid);
    person.setEmail(email);
    person.setOrcid(orcid);

    PureAPIName name = new PureAPIName();
    name.setFirstName(firstName);
    name.setLastName(lastName);
    person.setName(name);

    return person;
  }
}
