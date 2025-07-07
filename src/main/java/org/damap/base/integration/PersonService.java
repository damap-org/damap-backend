package org.damap.base.integration;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.damap.base.rest.PersonServiceBroker;
import org.damap.base.rest.base.ResultList;
import org.damap.base.rest.base.Search;
import org.damap.base.rest.dmp.domain.ContributorDO;

/**
 * This service queries persons from the customized backend for the frontend. There are multiple
 * implementations of this interface, being switched between them using the {@link
 * PersonServiceBroker}.
 */
public interface PersonService {
  /**
   * Read a person based on the ID.
   *
   * @param id a {@link java.lang.String} object the ID of the person being queried.
   * @return a person or null if no person was found.
   */
  default ContributorDO read(String id) {
    return read(id, new MultivaluedHashMap<>());
  }

  /**
   * Find a person based on the specified query parameters.
   *
   * @param query the query parameters.
   * @return a result list of persons matching the criteria.
   */
  default ResultList<ContributorDO> search(Search query) {
    return search(query.toMap());
  }

  // region Legacy methods

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
        "Bug: Calling the PersonService.convertValue function is no longer supported.");
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
        "Bug: Calling the PersonService.getEntityFields function is no longer supported.");
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
  default ContributorDO read(String id, MultivaluedMap<String, String> queryParams) {
    throw new RuntimeException(
        "Bug: Calling the PersonService.read function with a MultivaluedMap is deprecated "
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
  default ResultList<ContributorDO> search(MultivaluedMap<String, String> queryParams) {
    throw new RuntimeException(
        "Bug: Calling the PersonService.search function with a MultivaluedMap is "
            + "deprecated and should no longer be called. This function is only kept around to help customizations "
            + "transition to the new search function with a Search parameters. Please update your call to use the "
            + "newer search function.");
  }

  // endregion
}
