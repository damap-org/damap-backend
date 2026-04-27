package org.damap.base.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "instance_config")
@Table(name = "instance_config")
@EqualsAndHashCode(callSuper = true)
public class InstanceConfig extends PanacheEntity {

  @Column(name = "public_available", nullable = false)
  public Boolean publicAvailable;

  @Column(name = "consent_form_enabled", nullable = false)
  private Boolean consentFormEnabled;
}
