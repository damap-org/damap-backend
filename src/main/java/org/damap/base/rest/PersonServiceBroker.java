package org.damap.base.rest;

import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.extern.jbosslog.JBossLog;
import org.damap.base.integration.PersonService;
import org.damap.base.rest.config.domain.ServiceConfigDO;

/** PersonServiceBroker class. */
@JBossLog
@ApplicationScoped
public class PersonServiceBroker {

  LinkedHashMap<String, PersonService> personServices = new LinkedHashMap<>();

  /**
   * Constructor for PersonServiceBroker.
   *
   * @param config a {@link org.damap.base.rest.ConfigResource} object
   * @param availableServices a {@link java.util.List} object
   */
  @Inject
  public PersonServiceBroker(ConfigResource config, @All List<PersonService> availableServices) {
    List<ServiceConfigDO> configuredServices = config.config().getPersonSearchServiceConfigs();

    configuredServices.forEach(
        serviceConfig -> {
          boolean found = false;
          String configClassName = serviceConfig.getClassName();
          for (var service : availableServices) {
            try {
              String serviceClassName =
                  service.getClass().getCanonicalName().split("_ClientProxy")[0];

              if (configClassName.equals(serviceClassName)) {
                personServices.put(serviceConfig.getQueryValue(), service);
                found = true;
                break;
              }
            } catch (Exception e) {
              log.error(
                  String.format("Issue trying to initialize person service %s", configClassName),
                  e);
            }
          }
          if (!found) {
            log.warn(
                String.format("Service '%s' configured but is not available", configClassName));
          }
        });
  }

  /**
   * getServiceFromQueryParams.
   *
   * @param queryParams a {@link jakarta.ws.rs.core.MultivaluedMap} object
   * @return a {@link PersonService} object
   */
  public PersonService getServiceFromQueryParams(MultivaluedMap<String, String> queryParams) {
    return this.getServiceForQueryParam(queryParams.getFirst("searchService"));
  }

  /**
   * getServiceForQueryParam.
   *
   * @param searchServiceType a {@link java.lang.String} object
   * @return a {@link PersonService} object
   */
  public PersonService getServiceForQueryParam(String searchServiceType) {
    PersonService searchService = personServices.get(searchServiceType);
    if (searchService == null && !personServices.isEmpty()) {
      searchService = personServices.entrySet().iterator().next().getValue();
    }

    return searchService;
  }
}
