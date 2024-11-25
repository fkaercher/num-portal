package org.highmed.numportal.service.executors;

import org.highmed.numportal.domain.model.Cohort;
import org.highmed.numportal.domain.model.CohortGroup;
import org.highmed.numportal.service.ehrbase.EhrBaseService;
import org.highmed.numportal.service.exception.IllegalArgumentException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static org.highmed.numportal.domain.templates.ExceptionsTemplate.CANNOT_EXECUTE_AN_EMPTY_COHORT;

@Slf4j
@Service
@AllArgsConstructor
public class CohortExecutor {

  private final AqlCombiner aqlCombiner;

  private final AqlExecutor aqlExecutor;

  private final EhrBaseService ehrBaseService;

  public Set<String> execute(Cohort cohort, Boolean allowUsageOutsideEu) {

    if (cohort == null || cohort.getCohortGroup() == null) {
      throw new IllegalArgumentException(CohortExecutor.class, CANNOT_EXECUTE_AN_EMPTY_COHORT);
    }

    return executePatientIds(cohort.getCohortGroup(), allowUsageOutsideEu);
  }

  public Set<String> executePatientIds(CohortGroup cohortGroup, Boolean allowUsageOutsideEu) {
    var aqlWithParams = aqlCombiner.combineQuery(cohortGroup);
    var query = aqlExecutor.prepareQuery(aqlWithParams, allowUsageOutsideEu);
    if (query == null) {
      return Set.of();
    }
    return ehrBaseService.retrieveEligiblePatientIds(query);
  }

  public long executeNumberOfPatients(CohortGroup cohortGroup, Boolean allowUsageOutsideEu) {
    var aqlWithParams = aqlCombiner.combineQuery(cohortGroup);
    var query = aqlExecutor.prepareQuery(aqlWithParams, allowUsageOutsideEu);
    if (query == null) {
      return 0L;
    }
    return ehrBaseService.retrieveNumberOfPatients(query);
  }

  public Map<String, Integer> executeNumberOfPatientsPerPath(CohortGroup cohortGroup, Boolean allowUsageOutsideEu, String path) {
    var aqlWithParams = aqlCombiner.combineQuery(cohortGroup);
    var query = aqlExecutor.prepareQuery(aqlWithParams, allowUsageOutsideEu);
    if (query == null) {
      return Map.of();
    }
    return ehrBaseService.retrieveNumberOfPatientsPerPath(query, path);
  }
}
