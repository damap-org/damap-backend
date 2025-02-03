package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "technical_resource")
@Audited
public class TechnicalResource extends PanacheEntity {

  @Column(name = "name")
  @NotBlank(message = "technical resource name cannot be blank")
  @NotNull(message = "technical resource name cannot be null") private String name;

  @Column(name = "description")
  private String description;

  @ManyToOne private Dataset dataset;
}
