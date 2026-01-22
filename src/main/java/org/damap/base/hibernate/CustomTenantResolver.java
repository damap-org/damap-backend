package org.damap.base.hibernate;

import io.quarkus.arc.Unremovable;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.quarkus.security.ForbiddenException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.util.List;
import org.damap.base.security.SecurityService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@RequestScoped
@Unremovable
@PersistenceUnitExtension
public class CustomTenantResolver implements TenantResolver {

  private static final Logger LOG = Logger.getLogger(CustomTenantResolver.class);

  @ConfigProperty(name = "tenants", defaultValue = "default")
  List<String> tenantIds;

  @Inject SecurityService securityService;

  @Override
  public String getDefaultTenantId() {
    // this bootstrap db is used by hibernate during boot to build the persistence unit
    return "bootstrap";
  }

  @Override
  public String resolveTenantId() {
    // String tenantId = securityService.getTenantId();
    String tenantId = tenantIds.get(0);
    if (!tenantIds.contains(tenantId)) {
      throw new ForbiddenException("TenantId mismatch");
    }
    LOG.debug("TenantId = " + tenantId);
    return tenantId;
  }
}
