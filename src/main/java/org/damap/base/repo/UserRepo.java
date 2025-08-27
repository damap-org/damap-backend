package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import org.damap.base.domain.User;

@ApplicationScoped
public class UserRepo implements PanacheRepository<User> {

  public User findUserBySubject(String subject) {
    return find(
            "select user_ from damap_user user_ where user_.subject = :subject",
            Parameters.with("subject", subject))
        .firstResult();
  }

  public List<User> searchByNickname(String nicknamePart) {
    return find(
            "select user_ from damap_user user_ where lower(user_.nickname) like lower(concat('%', :nicknamePart, '%'))",
            Parameters.with("nicknamePart", nicknamePart))
        .list();
  }
}
