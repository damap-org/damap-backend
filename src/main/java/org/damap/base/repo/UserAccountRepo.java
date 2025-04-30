package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import org.damap.base.domain.User;

@ApplicationScoped
public class UserAccountRepo implements PanacheRepository<User> {

  public Optional<User> findByUserId(String userId) {
    return find("userId", userId).firstResultOptional();
  }

  public List<User> findAllUsersPage(int offset, int pagesize) {
    return findAll().page(Page.of(offset, pagesize)).list();
  }
}
