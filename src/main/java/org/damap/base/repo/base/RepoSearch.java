package org.damap.base.repo.base;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface RepoSearch<T> extends PanacheRepository<T> {

  // TODO: Add pagination
  /**
   * Search by parameters. Automatically generates a query based on the parameters and values in the
   * query parameters.
   *
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} object, containing the attribute
   *     as key and the values as values in an array
   * @return a {@link java.util.List} object, containing the results
   */
  default List<T> searchByParameters(MultivaluedMap<String, Object> queryParams) {

    StringBuilder query = new StringBuilder();

    Map<String, Object> params = new HashMap<>();

    int counter = 0;

    for (Map.Entry<String, List<Object>> entry : queryParams.entrySet()) {
      String key = entry.getKey();
      List<Object> values = entry.getValue();

      if (values.isEmpty()) {
        continue;
      }

      if (counter != 0) {
        query.append(" AND ");
      }

      query.append(" ( ");

      query.append(key).append(" = :").append(key).append("0");
      params.put(key + "0", values.get(0));
      for (int i = 1; i < values.size(); i++) {
        query.append(" OR %s = :%s".formatted(key, key + i));
        params.put(key + i, values.get(i));
      }
      query.append(" )");

      counter++;
    }

    return find(query.toString(), Sort.by("id"), params).list();
  }
}
