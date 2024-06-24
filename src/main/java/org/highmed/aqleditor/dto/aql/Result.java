package org.highmed.aqleditor.dto.aql;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class Result {

  private String q;

  @JsonProperty("query_parameters")
  private Map<String, String> queryParameters;
}
