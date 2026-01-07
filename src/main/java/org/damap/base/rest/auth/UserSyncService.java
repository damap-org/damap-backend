package org.damap.base.rest.auth;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.damap.base.domain.User;
import org.damap.base.repo.DamapUserRepo;

/**
 * This class is responsible for synchronizing user information from an external IDP (i.e Keycloak)
 * to the database. It checks if a user with the given identifier exists, and if not, it creates a
 * new user. If the user already exists, it updates the user's information. The return value of this
 * method is cached for 1-2 hours for instance and allows to invoke this method less frequently, as
 * the user information is not expected to change often (email, name, first name, last name).
 */
@ApplicationScoped
public class UserSyncService {

  @Inject DamapUserRepo userRepo;

  @CacheResult(cacheName = "user-sync-cache")
  @Transactional
  public String syncUser(
      String identifier, String email, String name, String firstName, String lastName) {

    User user = userRepo.findUserByIdentifier(identifier);

    if (user == null) {
      // Create new
      user =
          User.builder()
              .identifier(identifier)
              .email(email)
              .name(name)
              .firstName(firstName)
              .lastName(lastName)
              .build();
      userRepo.persist(user);
    } else {
      user.setEmail(email);
      user.setName(name);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      userRepo.persist(user);
    }

    return identifier;
  }
}
