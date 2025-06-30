package org.damap.base.rest.config.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.util.List;
import lombok.Data;

/** PersonServiceConfigurations class. */
@Data
public class PersonServiceConfigurations {
  List<ServiceConfig> configs;

  /**
   * Decodes a JSON string value into the current configuration.
   *
   * @param input a JSON {@link java.lang.String} value in the format of <code>
   *     {"person-services": [...]}</code> or directly as a person services configuration list.
   * @return a {@link org.damap.base.rest.config.domain.PersonServiceConfigurations} the decoded
   *     object
   * @throws java.io.IOException if any.
   */
  public static PersonServiceConfigurations of(String input) throws IOException {
    // We have to do a two-step decoding here because the
    // input value comes as {"person-services": [...]}.
    ObjectMapper mapper = new ObjectMapper();
    var jsonTree = mapper.readTree(input);
    String stringValue;
    if (jsonTree.has("person-services")) {
      // Config file, with prefix.
      var jsonConfigs = jsonTree.get("person-services");
      if (jsonConfigs instanceof TextNode) {
        stringValue = jsonConfigs.textValue();
      } else {
        stringValue = jsonConfigs.toString();
      }
    } else {
      // Value from environment variables, no prefix.
      stringValue = input;
    }

    PersonServiceConfigurations entity = new PersonServiceConfigurations();
    entity.configs = mapper.readValue(stringValue, new TypeReference<>() {});

    return entity;
  }
}
