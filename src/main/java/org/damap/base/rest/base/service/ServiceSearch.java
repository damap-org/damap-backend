package org.damap.base.rest.base.service;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.damap.base.rest.base.ResultList;

/**
 * Generic interface for searching objects.
 *
 * @deprecated this interface is too generic and should no longer be used. Create specific search
 *     interfaces instead.
 */
@Deprecated
public interface ServiceSearch<E> {
  /**
   * search.
   *
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} object
   * @return a {@link org.damap.base.rest.base.ResultList} object
   */
  ResultList<E> search(MultivaluedMap<String, String> queryParams);

  default Object convertValue(Class<?> type, String value) {
    if (type == Long.class) {
      return Long.parseLong(value);
    } else if (type == Boolean.class) {
      return Boolean.parseBoolean(value);
    } else if (type == String.class) {
      return value;
    } else {
      throw new IllegalArgumentException("Unsupported type: " + type);
    }
  }

  default MultivaluedMap<String, Class> getEntityFields() {
    return new MultivaluedHashMap<>();
  }
}
