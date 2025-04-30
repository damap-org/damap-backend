package org.damap.base.rest.account.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.domain.ApiKey;
import org.damap.base.domain.User;
import org.damap.base.domain.UserRole;
import org.damap.base.repo.ApiKeyRepo;
import org.damap.base.repo.UserAccountRepo;
import org.damap.base.rest.account.domain.ApiKeyDO;
import org.damap.base.rest.account.domain.UserDO;
import org.damap.base.rest.account.domain.UserPageDO;

@ApplicationScoped
@JBossLog
public class AccountService {

  @Inject private UserAccountRepo userAccountRepo;

  @Inject private ApiKeyRepo apiKeyRepo;

  public List<String> getApiKeys(String userId) {
    log.info("GET /account/api-keys");
    List<ApiKey> apiKeys = apiKeyRepo.findByUserId(userId);
    return apiKeys.stream().map(ApiKey::getName).toList();
  }

  @Transactional
  public ApiKeyDO createApiKey(
      String keyName, String username, String displayName, String userId, Set<String> roles) {
    log.info("POST /account/api-keys");

    if (keyName == null || keyName.isEmpty()) {
      throw new IllegalArgumentException("API key name cannot be null or empty");
    }

    Optional<User> optionalUser = userAccountRepo.findByUserId(userId);
    User existingUser;

    if (optionalUser.isPresent()) {
      existingUser = optionalUser.get();
    } else {
      existingUser = new User();
      existingUser.setUsername(username);
      existingUser.setDisplayName(displayName);
      existingUser.setUserId(userId);
      existingUser.persist();

      for (String role : roles) {
        UserRole userRole = new UserRole();
        userRole.setUser(existingUser);
        userRole.setRole(role);
        userRole.persist();
      }
    }

    Optional<ApiKey> optionalApiKey = apiKeyRepo.findByKey(keyName);
    if (optionalApiKey.isPresent()
        && Objects.equals(optionalApiKey.get().getUser().getUserId(), existingUser.getUserId())) {
      throw new IllegalArgumentException("API key already exists for this user with the same name");
    }

    ApiKey apiKey = new ApiKey();
    apiKey.setName(keyName);
    apiKey.setValue(java.util.UUID.randomUUID().toString());
    apiKey.setUser(existingUser);
    apiKey.persist();

    return ApiKeyDO.builder().name(apiKey.getName()).value(apiKey.getValue()).build();
  }

  @Transactional
  public void refreshUserApiKeyRole(
      String username, String displayName, String userId, Set<String> roles) {
    log.info("POST /account/refresh-user-role");
    Optional<User> optionalUser = userAccountRepo.findByUserId(userId);
    if (optionalUser.isPresent()) {
      User existingUser = optionalUser.get();
      existingUser.setUsername(username);
      existingUser.setDisplayName(displayName);
      existingUser.setRoles(
          roles.stream()
              .map(
                  role -> {
                    UserRole userRole = new UserRole();
                    userRole.setUser(existingUser);
                    userRole.setRole(role);
                    return userRole;
                  })
              .toList());
      existingUser.persist();
    } else {
      throw new NotFoundException("User not found");
    }
  }

  @Transactional
  public void deleteApiKey(String key, String userId) {
    log.info("DELETE /account/api-keys/" + key);

    if (key == null || key.isEmpty()) {
      throw new IllegalArgumentException("API key cannot be null or empty");
    }

    Optional<ApiKey> optionalApiKey = apiKeyRepo.findByKey(key);
    if (optionalApiKey.isPresent()) {
      ApiKey apiKey = optionalApiKey.get();
      if (Objects.equals(apiKey.getUser().getUserId(), userId)) {
        apiKeyRepo.delete(apiKey);
      } else {
        throw new ForbiddenException("API key does not belong to the user");
      }
    } else {
      throw new NotFoundException("API key not found");
    }
  }

  public UserPageDO getAllUsers(int offset, int limit) {
    log.info("GET /account/users page: " + offset + ", size: " + limit);
    List<User> allUsers = userAccountRepo.findAllUsersPage(offset, limit);

    List<UserDO> users =
        allUsers.stream()
            .map(
                user ->
                    UserDO.builder()
                        .userId(user.getUserId())
                        .displayName(user.getDisplayName())
                        .username(user.getUsername())
                        .roles(user.getRoles().stream().map(UserRole::getRole).toList())
                        .apiKeys(user.getApiKeys().stream().map(ApiKey::getName).toList())
                        .build())
            .toList();

    int totalElements = (int) userAccountRepo.count();
    int totalPages = (int) Math.ceil((double) totalElements / limit);
    return UserPageDO.builder()
        .users(users)
        .totalElements(totalElements)
        .totalPages(totalPages)
        .size(limit)
        .number(offset / limit + 1)
        .build();
  }

  @Transactional
  public boolean resetAllApiKeys(String userId) {
    List<ApiKey> apiKeys = apiKeyRepo.findByUserId(userId);
    for (ApiKey apiKey : apiKeys) {
      apiKeyRepo.delete(apiKey);
    }
    apiKeyRepo.flush();
    return true;
  }
}
