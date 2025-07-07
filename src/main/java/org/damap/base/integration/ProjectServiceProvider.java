package org.damap.base.integration;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.List;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;
import org.damap.base.rest.dmp.domain.ProjectDO;
import org.damap.base.rest.dmp.domain.ProjectSupplementDO;

/** This interface describes the necessary endpoint to provide a project integration. */
public interface ProjectServiceProvider {
  /**
   * @return the identifier the user can configure this service by.
   */
  default String getConfigID() {
    return "default";
  }

  /**
   * Return a list of all staff related to a project.
   *
   * @param projectId a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  List<ContributorDO> getProjectStaff(String projectId);

  /**
   * Get supplemental project information for a specific project.
   *
   * @param projectId a {@link java.lang.String} object
   * @return a {@link ProjectSupplementDO} object
   */
  ProjectSupplementDO getProjectSupplement(String projectId);

  /**
   * Get the leader of a project.
   *
   * @param projectId a {@link java.lang.String} object
   * @return a {@link org.damap.base.rest.dmp.domain.ContributorDO} object
   */
  ContributorDO getProjectLeader(String projectId);

  /**
   * Get recommended projects based on a query value.
   *
   * @param search the search parameters for recommended projects.
   * @return a {@link org.damap.base.rest.base.ResultList} object
   */
  default ResultList<ProjectDO> getRecommended(Search search) {
    return getRecommended(search.toMap());
  }

  /**
   * Read a project based on the ID.
   *
   * @param id a {@link java.lang.String} object the ID of the person being queried.
   * @return a person or null if no person was found.
   */
  default ProjectDO read(String id) {
    return read(id, new MultivaluedHashMap<>());
  }

  /**
   * Find a project based on the specified query parameters.
   *
   * @param query the query parameters.
   * @return a result list of persons matching the criteria.
   */
  default ResultList<ProjectDO> search(Search query) {
    return search(query.toMap());
  }

  // region Legacy methods
  /**
   * Get recommended projects based on a query value.
   *
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} object
   * @return a {@link org.damap.base.rest.base.ResultList} object
   * @deprecated this method allows a two broad set of undocumented parameters to be passed. Please
   *     use and implement the variant accepting a {@link Search} object instead.
   */
  @Deprecated
  default ResultList<ProjectDO> getRecommended(MultivaluedMap<String, String> queryParams) {
    throw new RuntimeException(
        "Bug: Calling the ProjectServiceProvider.getRecommended function with a MultivaluedMap is "
            + "deprecated and should no longer be called. This function is only kept around to help customizations "
            + "transition to the new function with the Search parameter. Please update your call to use the newer "
            + "getRecommended function.");
  }

  /**
   * Helper function to convert a string to a limited set of types.
   *
   * @throws RuntimeException always.
   * @deprecated this method should no longer be used as it was misplaced in the interface in the
   *     first place.
   */
  @Deprecated
  default Object convertValue(Class<?> ignoredType, String ignoredValue) {
    throw new RuntimeException(
        "Bug: Calling the ProjectServiceProvider.convertValue function is no longer supported.");
  }

  /**
   * A helper method to return the fields of the entity.
   *
   * @throws RuntimeException always.
   * @deprecated this method should no longer be used as it was misplaced in the interface in the
   *     first place.
   */
  @Deprecated
  default MultivaluedMap<String, Class<?>> getEntityFields() {
    throw new RuntimeException(
        "Bug: Calling the ProjectServiceProvider.getEntityFields function is no longer supported.");
  }

  /**
   * Returns a single person matching either the specified ID. The query parameters are not used in
   * this request and should not be read or populated.
   *
   * @param id a {@link java.lang.String} identifier for the object.
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} containing the query parameters
   *     from the request. Do not use this field in your implementation.
   * @return the retrieved person or null if none was found.
   */
  @Deprecated
  default ProjectDO read(String id, MultivaluedMap<String, String> queryParams) {
    throw new RuntimeException(
        "Bug: Calling the ProjectServiceProvider.read function with a MultivaluedMap is deprecated "
            + "and should no longer be called. This function is only kept around to help customizations transition "
            + "to the new search function with only the ID parameter. Please update your call to use the newer "
            + "read function.");
  }

  /**
   * Search for one or more person based on the specified query parameters.
   *
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} object the query parameters. See
   *     {@link Search} for details on possible parameters.
   * @return Zero or more ContributorDO objects matching the criteria.
   * @deprecated call and implement the search method with the {@link Search} object as a parameter
   *     instead.
   */
  @Deprecated
  default ResultList<ProjectDO> search(MultivaluedMap<String, String> queryParams) {
    throw new RuntimeException(
        "Bug: Calling the ProjectServiceProvider.search function with a MultivaluedMap is "
            + "deprecated and should no longer be called. This function is only kept around to help customizations "
            + "transition to the new search function with a Search parameters. Please update your call to use the "
            + "newer search function.");
  }

  // endregion
}
