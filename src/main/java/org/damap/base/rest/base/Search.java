package org.damap.base.rest.base;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import lombok.Data;

/** Search class. */
@Data
public class Search {
  private Pagination pagination = new Pagination();
  private String query;

  /**
   * fromMap.
   *
   * @param map a {@link jakarta.ws.rs.core.MultivaluedMap} object
   * @return a {@link org.damap.base.rest.base.Search} object
   */
  public static Search fromMap(MultivaluedMap<String, String> map) {
    Search search = new Search();
    search.pagination = Pagination.fromMap(map);
    search.query = map.getFirst("q");

    return search;
  }

  /**
   * Converts the search back to a {@link jakarta.ws.rs.core.MultivaluedMap} for legacy API calls.
   *
   * @return the map containing the query string.
   */
  public MultivaluedMap<String, String> toMap() {
    MultivaluedMap<String, String> map = new MultivaluedHashMap<>();
    if (query != null) {
      map.add("q", query);
    }
    MultivaluedMap<String, String> paginationMap = pagination.toMap();
    for (String key : paginationMap.keySet()) {
      map.addAll(key, paginationMap.get(key));
    }
    return map;
  }
}
