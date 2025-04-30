package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.damap.base.domain.ApiKey;
import org.damap.base.domain.User;

@ApplicationScoped
public class ApiKeyRepo implements PanacheRepository<ApiKey> {

  public Optional<ApiKey> findByKey(String key) {
    return find("name", key).firstResultOptional();
  }

  @Transactional
  public Optional<User> findUserByApiKeyValue(String value) {
    return find("value", value).firstResultOptional().map(ApiKey::getUser);
  }

  public List<ApiKey> findByUserId(String userId) {
    return find("user.userId", userId).list();
  }
}
