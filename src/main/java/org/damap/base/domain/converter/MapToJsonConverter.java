package org.damap.base.domain.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Converter(autoApply = false)
public class MapToJsonConverter implements AttributeConverter<Map<String, String>, String> {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Map<String, String> attribute) {
    if (attribute == null) return null;
    try {
      return MAPPER.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Could not serialize map to string", e);
    }
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) return Collections.emptyMap();
    try {
      return MAPPER.readValue(
          dbData, MAPPER.getTypeFactory().constructMapType(Map.class, String.class, String.class));
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not deserialize string to map", e);
    }
  }
}
