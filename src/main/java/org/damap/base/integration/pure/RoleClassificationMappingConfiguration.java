package org.damap.base.integration.pure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import lombok.Data;
import org.damap.base.enums.EContributorRole;

/** A workaround for Quarkus configs being strictly string-string. */
@Data
public class RoleClassificationMappingConfiguration {
  Map<String, EContributorRole> configs;

  /**
   * A workaround for Quarkus configs being strictly string-string.
   *
   * @param input a {@link java.lang.String} object
   * @return a {@link org.damap.base.rest.config.domain.PersonServiceConfigurations} object
   * @throws java.io.IOException if any.
   */
  public static RoleClassificationMappingConfiguration of(String input) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    RoleClassificationMappingConfiguration entity = new RoleClassificationMappingConfiguration();
    entity.configs = mapper.readValue(input, new TypeReference<>() {});

    return entity;
  }
}
