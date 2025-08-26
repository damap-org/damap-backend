package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "recommended_repository")
public class RecommendedRepository extends PanacheEntity {

  @NotBlank
  @Size(max = 255)
  @Column(name = "repository_id", nullable = false, unique = true)
  private String repositoryId;
}
