package org.highmed.numportal.service.executors;

import org.highmed.numportal.domain.model.Cohort;
import org.highmed.numportal.domain.model.CohortAql;
import org.highmed.numportal.domain.model.CohortGroup;
import org.highmed.numportal.domain.model.Operator;
import org.highmed.numportal.domain.model.Type;
import org.highmed.numportal.service.exception.IllegalArgumentException;

import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CohortExecutorTest {

  private final String COHORT_NAME = "Cohort name";
  private final AqlQuery QUERY = new AqlQuery();
  private final String AQL_NAME = "AQL query name";
  private final String AQL_QUERY = "SELECT A ... FROM E ... WHERE ...";

  @Mock
  private AqlCombiner aqlCombiner;
  @Mock
  private AqlExecutor aqlExecutor;
  @InjectMocks
  private CohortExecutor cohortExecutor;

  @Test
  public void shouldCorrectlyExecuteAndCohort() {

    CohortAql cohortAql1 = CohortAql.builder().id(1L).name(AQL_NAME).query(AQL_QUERY).build();
    CohortAql cohortAql2 = CohortAql.builder().id(2L).name(AQL_NAME).query(AQL_QUERY).build();

    AqlWithParams aql = new AqlWithParams(QUERY, Map.of("p1", 1));
    when(aqlExecutor.execute(aql, false))
            .thenReturn(Set.of("1", "2", "5", "10"));

    CohortGroup first =
        CohortGroup.builder().type(Type.AQL).query(cohortAql1).parameters(Map.of("p1", 1)).build();
    CohortGroup second =
        CohortGroup.builder().type(Type.AQL).query(cohortAql2).parameters(Map.of("p1", 1)).build();

    CohortGroup andCohort =
        CohortGroup.builder()
            .type(Type.GROUP)
            .operator(Operator.AND)
            .children(List.of(first, second))
            .build();

    Cohort cohort = Cohort.builder().name(COHORT_NAME).cohortGroup(andCohort).build();

    when(aqlCombiner.combineQuery(andCohort)).thenReturn(aql);

    Set<String> result = cohortExecutor.execute(cohort, false);

    assertThat(result, notNullValue());
    System.out.println(result);
    assertThat(result, is(Set.of("1", "2", "5", "10")));
  }

  @Test
  public void shouldCorrectlyExecuteOrCohort() {
    CohortAql cohortAql1 = CohortAql.builder().id(1L).name(AQL_NAME).query(AQL_QUERY).build();
    CohortAql cohortAql2 = CohortAql.builder().id(2L).name(AQL_NAME).query(AQL_QUERY).build();

    AqlWithParams aql = new AqlWithParams(QUERY, Map.of("p1", 1));
    when(aqlExecutor.execute(aql, false))
            .thenReturn(Set.of("1", "2", "5", "10"));

    CohortGroup first =
        CohortGroup.builder().type(Type.AQL).query(cohortAql1).parameters(Map.of("p1", 1)).build();
    CohortGroup second =
        CohortGroup.builder().type(Type.AQL).query(cohortAql2).parameters(Map.of("p1", 1)).build();

    CohortGroup orCohort =
        CohortGroup.builder()
            .type(Type.GROUP)
            .operator(Operator.OR)
            .children(List.of(first, second))
            .build();

    Cohort cohort = Cohort.builder().name(COHORT_NAME).cohortGroup(orCohort).build();

    when(aqlCombiner.combineQuery(orCohort)).thenReturn(aql);

    Set<String> result = cohortExecutor.execute(cohort, false);

    assertThat(result, notNullValue());
    assertThat(result, is(Set.of("1", "2", "5", "10")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldHandleNullCohort() {
    cohortExecutor.execute(null, false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldHandleNullCohortGroup() {
    Cohort cohort = Cohort.builder().name(COHORT_NAME).cohortGroup(null).build();
    cohortExecutor.execute(cohort, false);
  }
}
