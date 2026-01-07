package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "damap_user")
@Table(name = "damap_user")
@EqualsAndHashCode(callSuper = true)
public class User extends PanacheEntity {

  @Column(name = "identifier", unique = true, nullable = false)
  private String identifier;

  @Column(name = "name")
  private String name;

  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "email")
  private String email;
}
