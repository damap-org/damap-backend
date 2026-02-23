package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "export_template")
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"data"})
public class ExportTemplate extends PanacheEntity {

  @Column(name = "name", nullable = false)
  private String name; // e.g., "Standard FWF" or "Science Europe - Special Version"

  @Column(name = "template_key", nullable = false)
  private String templateKey; // "FWF", "HORIZON", or "SCIENCE_EUROPE"

  @Lob
  @Basic(fetch = FetchType.LAZY)
  @Column(name = "data")
  private byte[] data; // Null for standard defaults (loaded from resources), non-null for custom

  @Column(name = "active", nullable = false)
  private boolean active;

  @Column(name = "is_custom", nullable = false)
  private boolean isCustom;
}
