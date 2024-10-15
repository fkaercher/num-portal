package org.highmed.numportal.service.executors;

import org.highmed.numportal.domain.model.Cohort;
import org.highmed.numportal.domain.model.CohortGroup;
import org.highmed.numportal.domain.model.Type;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.highmed.numportal.service.ehrbase.EhrBaseService;
import org.highmed.numportal.service.exception.IllegalArgumentException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.highmed.numportal.domain.templates.ExceptionsTemplate.CANNOT_EXECUTE_AN_EMPTY_COHORT;

@Slf4j
@Service
@AllArgsConstructor
public class CohortExecutor {

  private final SetOperationsService setOperations;

  private final AqlExecutor aqlExecutor;

  private final EhrBaseService ehrBaseService;

  public Set<String> execute(Cohort cohort, Boolean allowUsageOutsideEu) {

    if (cohort == null || cohort.getCohortGroup() == null) {
      throw new IllegalArgumentException(CohortExecutor.class, CANNOT_EXECUTE_AN_EMPTY_COHORT);
    }

    return executeGroup(cohort.getCohortGroup(), allowUsageOutsideEu);
  }

  public Set<String> executeGroup(CohortGroup cohortGroup, Boolean allowUsageOutsideEu) {
    if (cohortGroup.getType() == Type.GROUP) {

      List<Set<String>> sets =
          cohortGroup.getChildren().stream()
              .map(e -> executeGroup(e, allowUsageOutsideEu))
              .collect(Collectors.toList());

      return setOperations.apply(
          cohortGroup.getOperator(), sets, ehrBaseService.getAllPatientIds());

    } else if (cohortGroup.getType() == Type.AQL) {
      return aqlExecutor.execute(
          cohortGroup.getQuery(), cohortGroup.getParameters(), allowUsageOutsideEu);
    }

    return SetUtils.emptySet();
  }

  public Map<String, Set<String>> executeGroupAft(CohortGroup cohortGroup, Boolean allowUsageOutsideEu) {///
    if (cohortGroup.getType() == Type.GROUP) {

      List<Map<String, Set<String>>> list =
          cohortGroup.getChildren().stream()
              .map(e -> executeGroupAft(e, allowUsageOutsideEu))
              .toList();

      if (list.isEmpty()) {
        return Map.of();
      } else {
        return list.get(0).keySet().stream().collect(
                HashMap::new,
                (map, location) -> map.put(location,
                        setOperations.apply(
                                cohortGroup.getOperator(), list.stream().map(e -> e.get(location)).toList(), ehrBaseService.getAllPatientIds2().get(location))),
                Map::putAll
        );
      }

    } else if (cohortGroup.getType() == Type.AQL) {
      return aqlExecutor.executeAft(
          cohortGroup.getQuery(), cohortGroup.getParameters(), allowUsageOutsideEu);
    }

    return Map.of();
  }
}
