package org.damap.base.integration.generic;

import io.quarkus.cache.CacheKeyGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.damap.base.security.SecurityService;

/**
 * Cache key generator for Generic Cris caches. Prepends tenant ID to every key so cache entries are
 * isolated per tenant.
 */
@ApplicationScoped
class GenericCrisCacheKeyGenerator implements CacheKeyGenerator {

  @Inject SecurityService securityService;

  @Override
  public Object generate(Method method, Object... methodParams) {
    List<Object> key = new ArrayList<>();
    String affiliation = securityService.getAffiliation();
    key.add(affiliation != null ? affiliation : "single-tenant");
    if ("getRecommended".equals(method.getName())) {
      key.add(securityService.getUserId());
    }
    Collections.addAll(key, methodParams);
    return key;
  }
}
