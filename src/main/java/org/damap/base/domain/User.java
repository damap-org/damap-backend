package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"apiKeys", "roles"})
@Entity
@Table(name = "user_account")
public class User extends PanacheEntity {

  @Column(name = "user_id", nullable = false, unique = true)
  private String userId;

  @Column(name = "display_name")
  private String displayName;

  @Column(nullable = false, unique = true)
  private String username;

  @OneToMany(mappedBy = "user")
  private List<ApiKey> apiKeys;

  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
  private List<UserRole> roles;
}
