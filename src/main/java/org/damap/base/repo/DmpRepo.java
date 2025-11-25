package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.damap.base.domain.Dmp;

/** DmpRepo class. */
@ApplicationScoped
public class DmpRepo implements PanacheRepository<Dmp> {

  /**
   * getAll.
   *
   * @return a {@link java.util.List} object
   */
  public List<Dmp> getAll() {
    return listAll(Sort.descending("created"));
  }
}
