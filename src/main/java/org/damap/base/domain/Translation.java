package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.hibernate.Length;
import org.hibernate.envers.Audited;

/** Translation class. */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "translation",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"key", "language"})},
    indexes = {@Index(name = "idx_translation_language_active", columnList = "language, active")})
@Audited
public class Translation extends PanacheEntity {

  @Version
  @Setter(AccessLevel.NONE)
  private long version;

  @Column(name = "key", nullable = false, length = 255)
  private String key;

  @Column(name = "language", nullable = false, length = 2)
  private String language;

  @Column(name = "default_value", nullable = false, length = Length.LONG32)
  private String defaultValue;

  @Column(name = "value", length = Length.LONG32)
  private String value;

  @Column(name = "active", nullable = false)
  private Boolean active = true;
}
