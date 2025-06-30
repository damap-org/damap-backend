package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.damap.base.domain.Access;
import org.damap.base.domain.Dmp;

/** AccessRepo class. */
@ApplicationScoped
public class AccessRepo implements PanacheRepository<Access> {

  /**
   * getAllByUniversityId.
   *
   * @param universityId a {@link java.lang.String} object
   * @return a {@link java.util.List} object
   */
  public List<Access> getAllByUniversityId(String universityId) {
    return list(
        "select access from Access access" + " where access.universityId = :universityId ",
        Parameters.with("universityId", universityId));
  }

  /**
   * getAccessByDmp.
   *
   * @param dmp a {@link org.damap.base.domain.Dmp} object
   * @return a {@link java.util.List} object
   */
  public List<Access> getAccessByDmp(Dmp dmp) {
    return list(
        "select access from Access access" + " where access.dmp = :dmp ",
        Parameters.with("dmp", dmp));
  }
}
