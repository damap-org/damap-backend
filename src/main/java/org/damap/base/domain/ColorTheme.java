package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.damap.base.domain.converter.MapToJsonConverter;
import org.hibernate.Length;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "color_theme")
public class ColorTheme extends PanacheEntity {

  @Column(name = "exact_colors", nullable = false)
  private boolean exactColors;

  @Convert(converter = MapToJsonConverter.class)
  @Column(name = "colors", length = Length.LONG32)
  private Map<String, String> colors = new HashMap<>();
}
