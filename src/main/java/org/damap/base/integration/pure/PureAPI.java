package org.damap.base.integration.pure;

import java.util.ArrayList;
import java.util.List;

/**
 * Function definitions for the Elsevier Pure API.
 *
 * @see <a href="https://api.elsevierpure.com/ws/api/rapidoc.html">Elsevier Pure API doc</a>
 */
interface PureAPI {
  /**
   * List all projects using pagination.
   *
   * @param size the size of the page.
   * @param offset the offset to start at.
   * @return the response containing the projects on that page.
   */
  PureAPIPaginatedProjectsResponse listAllProjects(Long size, Long offset);

  /**
   * Retrieve a project with a specific ID.
   *
   * @param uuid the ID of the project.
   * @return the project if found, or null if the project was not found.
   */
  PureAPIProject getProject(String uuid);

  /**
   * Retrieve all persons in the database, paginated.
   *
   * @param size the size of the page.
   * @param offset the offset to start at.
   * @return the response containing the list of persons.
   */
  PureAPIPaginatedPersonsResponse listAllPersons(Long size, Long offset);

  /**
   * Fetch a single person based on their ID.
   *
   * @param uuid the ID of the person to fetch.
   * @return the person if found, or null if the person was not found.
   */
  PureAPIPerson getPerson(String uuid);

  /**
   * Search projects using pagination.
   *
   * @param q query string, passed to Pure API {@code q} parameter
   * @param size the size of the page.
   * @param offset the offset to start at.
   * @return the response containing the projects on that page.
   */
  PureAPIPaginatedProjectsResponse searchProjects(String q, Long size, Long offset);

  /**
   * Retrieve all projects, sending multiple queries to the Pure API.
   *
   * @return a list of all projects in the Pure database.
   */
  default List<PureAPIProject> listAllProjects() {
    List<PureAPIProject> items = new ArrayList<>();
    long offset = 0;
    long pageSize = 100;
    boolean finished = false;
    while (!finished) {
      PureAPIPaginatedProjectsResponse projects = listAllProjects(pageSize, offset);
      if (projects == null || projects.items == null || projects.pageInformation == null) {
        break;
      }
      items.addAll(projects.items);
      offset = projects.pageInformation.offset + projects.pageInformation.size;
      if (projects.count <= offset) {
        finished = true;
      }
    }
    return items;
  }

  /**
   * Search projects, sending multiple queries to the Pure API.
   *
   * @param q query string, passed to Pure API {@code q} parameter
   * @return a list of matching projects
   */
  default List<PureAPIProject> searchAllProjects(String q) {
    List<PureAPIProject> items = new ArrayList<>();
    long offset = 0;
    long pageSize = 100;
    boolean finished = false;
    while (!finished) {
      PureAPIPaginatedProjectsResponse projects = searchProjects(q, pageSize, offset);
      if (projects == null || projects.items == null || projects.pageInformation == null) {
        break;
      }
      items.addAll(projects.items);
      offset = projects.pageInformation.offset + projects.pageInformation.size;
      if (projects.count <= offset) {
        finished = true;
      }
    }
    return items;
  }

  /**
   * Retrieve all persons, sending multiple queries to the Pure API.
   *
   * @return a list of all persons in the Pure database.
   */
  default List<PureAPIPerson> listAllPersons() {
    List<PureAPIPerson> items = new ArrayList<>();
    long offset = 0;
    long pageSize = 100;
    boolean finished = false;
    while (!finished) {
      PureAPIPaginatedPersonsResponse persons = listAllPersons(pageSize, offset);
      if (persons == null || persons.items == null || persons.pageInformation == null) {
        break;
      }
      items.addAll(persons.items);
      offset = persons.pageInformation.offset + persons.pageInformation.size;
      if (persons.count <= offset) {
        finished = true;
      }
    }
    return items;
  }
}
