package org.highmed.aqleditor.dto.aql;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class QueryValidationResponse {

  private boolean valid;
  private String message;
  private String startLine;
  private String startColumn;
  private String error;

}
