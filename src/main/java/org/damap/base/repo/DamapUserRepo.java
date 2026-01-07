package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.damap.base.domain.User;

@ApplicationScoped
public class DamapUserRepo implements PanacheRepository<User> {

  /** Find user list by match in name or email. */
  public List<User> findUsersByNameOrEmailMatch(String match) {
    if (match == null || match.isBlank()) {
      return List.of();
    }
    String pattern = "%" + match.trim().toLowerCase() + "%";

    return find(
            "lower(name) like :pattern or lower(email) like :pattern",
            Parameters.with("pattern", pattern))
        .page(Page.ofSize(20))
        .list();
  }

  public User findUserByIdentifier(String identifier) {
    return find("identifier", identifier).firstResult();
  }
}
