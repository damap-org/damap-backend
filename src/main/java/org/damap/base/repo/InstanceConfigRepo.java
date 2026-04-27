package org.damap.base.repo;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.damap.base.domain.InstanceConfig;

@ApplicationScoped
public class InstanceConfigRepo implements PanacheRepositoryBase<InstanceConfig, Long> {

  private static final Long CONFIG_ID = 1L;

  public InstanceConfig getConfig() {
    return findById(CONFIG_ID);
  }
}
