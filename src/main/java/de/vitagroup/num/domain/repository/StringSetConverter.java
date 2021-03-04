package de.vitagroup.num.domain.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Set;
import javax.persistence.AttributeConverter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
@AllArgsConstructor
public class StringSetConverter implements AttributeConverter<Set<String>, String> {

  private final ObjectMapper mapper;

  @Override
  public String convertToDatabaseColumn(Set<String> strings) {

    String stringsJson = null;
    try {
      stringsJson = mapper.writeValueAsString(strings);
    } catch (final JsonProcessingException e) {
      log.error("Cannot convert map to JSON", e);
    }

    return stringsJson;
  }

  @Override
  public Set<String> convertToEntityAttribute(String stringsJson) {

    if (StringUtils.isEmpty(stringsJson)) {
      return null;
    }

    Set<String> strings = null;
    try {
      strings = mapper.readValue(stringsJson, Set.class);
    } catch (final IOException e) {
      log.error("Cannot convert JSON to map", e);
    }

    return strings;
  }
}
