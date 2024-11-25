package org.highmed.numportal.service.executors;

import org.highmed.numportal.properties.ConsentProperties;
import org.highmed.numportal.service.policy.EuropeanConsentPolicy;
import org.highmed.numportal.service.policy.ProjectPolicyService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.render.AqlRenderer;
import org.ehrbase.openehr.sdk.aql.util.AqlUtil;
import org.ehrbase.openehr.sdk.generator.commons.aql.parameter.ParameterValue;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class AqlExecutor {

  private final ProjectPolicyService projectPolicyService;

  private final ConsentProperties consentProperties;

  public String prepareQuery(AqlWithParams aqlWithParams, Boolean allowUsageOutsideEu) {

    if (aqlWithParams != null && aqlWithParams.aqlQuery != null) {
      if (BooleanUtils.isTrue(allowUsageOutsideEu) || allowUsageOutsideEu == null) {
        applyPolicy(aqlWithParams);
      }

      String query = AqlRenderer.render(aqlWithParams.getAqlQuery());
      query = removeNullParameters(aqlWithParams.parameters, query);
      query = addParameters(aqlWithParams.parameters, query);

      return query;
    } else {
      return null;
    }
  }

  private void applyPolicy(AqlWithParams aqlWithParams) {
    AqlQuery aql = aqlWithParams.aqlQuery;
    projectPolicyService.apply(
        aql,
        List.of(
            EuropeanConsentPolicy.builder()
                                 .oid(consentProperties.getAllowUsageOutsideEuOid())
                                 .build()));

    aqlWithParams.aqlQuery = aql;
  }

  private String addParameters(Map<String, Object> parameters, String query) {
    if (MapUtils.isNotEmpty(parameters) && StringUtils.isNotEmpty(query)) {
      for (var v : getParameterValues(parameters)) {
        query = query.replace(v.getParameter().getAqlParameter(), v.buildAql());
      }
    }
    return query;
  }

  private String removeNullParameters(Map<String, Object> parameters, String query) {
    if (MapUtils.isNotEmpty(parameters) && StringUtils.isNotEmpty(query)) {

      Iterator<Map.Entry<String, Object>> iterator = parameters.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<String, Object> entry = iterator.next();
        if (entry.getValue() == null) {
          query = AqlUtil.removeParameter(query, entry.getKey());
          iterator.remove();
        }
      }
    }
    return query;
  }

  private List<ParameterValue> getParameterValues(Map<String, Object> parameters) {
    List<ParameterValue> parameterValues = new LinkedList<>();
    parameters.forEach((k, v) -> parameterValues.add(new ParameterValue(k, v)));
    return parameterValues;
  }
}
