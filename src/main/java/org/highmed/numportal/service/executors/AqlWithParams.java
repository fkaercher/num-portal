package org.highmed.numportal.service.executors;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;

import java.util.Map;

@AllArgsConstructor
@Data
public class AqlWithParams {
  AqlQuery aqlQuery;
  Map<String, Object> parameters;
}
