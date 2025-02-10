package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

/** Banner class. */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Audited
public class Banner extends PanacheEntity {

  @Column(name = "title")
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "dismissible")
  private Boolean dismissible;

  @Column(name = "color")
  private String color;
}
