package org.damap.base.integration.pure;

import io.quarkus.cache.CacheKeyGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.damap.base.security.SecurityService;

/**
 * Cache key generator for Pure API caches. Prepends tenant ID to every key so cache entries are
 * isolated per tenant. For {@code getRecommended}, also includes the user ID since results are
 * filtered per user.
 *
 * <p>Caching is applied at the {@link PureProjectService} level rather than on the {@link PureAPI}
 * interface because Quarkus REST client proxies do not support CDI interceptors on default methods.
 */
@ApplicationScoped
class PureCacheKeyGenerator implements CacheKeyGenerator {

  @Inject SecurityService securityService;

  @Override
  public Object generate(Method method, Object... methodParams) {
    List<Object> key = new ArrayList<>();
    String affiliation = securityService.getAffiliation();
    key.add(affiliation != null ? affiliation : "single-tenant");
    if ("getRecommended".equals(method.getName())) {
      key.add(securityService.getUserId());
    }
    for (Object param : methodParams) {
      key.add(param);
    }
    return key;
  }
}
