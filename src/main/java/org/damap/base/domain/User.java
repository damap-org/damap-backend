package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;

/** Entity representing a user in the DAMAP system. Is built from the IDP user info or JWT. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "damap_user")
@Table(name = "damap_user")
public class User extends PanacheEntity {

  @Column(name = "subject")
  private String subject;

  @Column(name = "nickname")
  private String nickname;

  @Column(name = "email")
  private String email;

  // This field indicates if the user has admin privileges
  // We consider this field as the source of truth for admin status
  // We only set it a first time initially for the "keycloak" IDP
  // mode, afterward it is managed internally
  @Column(name = "is_admin")
  private boolean isAdmin;
}
