package org.damap.base.rest.base.service;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Generic service to read an object.
 *
 * @deprecated this service is too generic and creates a leaky abstraction where all query
 *     parameters are passed down through the interface. Do not use.
 */
@Deprecated
public interface ServiceRead<E> {
  /**
   * Read the object E with the specified query parameters.
   *
   * @param id a {@link java.lang.String} identifier for the object.
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} containing the query parameters
   *     from the request.
   * @return the retrieved object, or null if no object was found.
   */
  E read(String id, MultivaluedMap<String, String> queryParams);

  /**
   * Get the object E with the specified id.
   *
   * @param id a {@link java.lang.String} object
   * @return a E object
   */
  default E read(String id) {
    return read(id, new MultivaluedHashMap<>());
  }
}
