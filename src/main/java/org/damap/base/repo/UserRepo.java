package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.damap.base.domain.User;

@ApplicationScoped
public class UserRepo implements PanacheRepository<User> {

  /**
   * Find user by subject field
   *
   * @param subject
   * @return
   */
  public User findUserBySubject(String subject) {
    return find(
            "select user_ from damap_user user_ where user_.subject = :subject",
            Parameters.with("subject", subject))
        .firstResult();
  }
}
