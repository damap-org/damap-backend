package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.Length;
import org.hibernate.envers.Audited;

/** Translation class. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
    name = "translation",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"key", "language"})},
    indexes = {@Index(name = "idx_translation_language_active", columnList = "language, active")})
@Audited
public class Translation extends PanacheEntityBase {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Version private long version;

  @Column(name = "key", nullable = false, length = 255)
  @NonNull @NotEmpty
  @Size(min = 1, max = 255)
  private String key;

  @Column(name = "language", nullable = false, length = 2)
  @NonNull @NotEmpty
  @Size(min = 2, max = 2)
  private String language;

  @Column(name = "default_value", nullable = false, length = Length.LONG32)
  @NonNull @NotEmpty
  @Size(min = 1, max = Length.LONG32)
  private String defaultValue;

  @Column(name = "value", length = Length.LONG32)
  @Size(max = Length.LONG32)
  private String value;

  @Column(name = "active", nullable = false)
  @NonNull private Boolean active = true;
}
