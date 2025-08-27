package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@Entity(name = "damap_user")
@Table(name = "damap_user")
public class User extends PanacheEntity {

  @Column(name = "subject")
  private String subject;

  @Column(name = "nickname")
  private String nickname;

  @Column(name = "email")
  private String email;

  @Column(name = "isadmin")
  private boolean isAdmin;
}
