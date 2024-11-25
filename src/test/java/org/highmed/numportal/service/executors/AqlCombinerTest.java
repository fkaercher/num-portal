package org.highmed.numportal.service.executors;

import org.highmed.numportal.domain.model.CohortAql;
import org.highmed.numportal.domain.model.CohortGroup;
import org.highmed.numportal.domain.model.Operator;
import org.highmed.numportal.domain.model.Type;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AqlCombinerTest {

  @InjectMocks
  private AqlCombiner aqlCombiner;

  private final String AQL_NAME = "AQL query name";

  @Test
  public void combineOneQuery() {
    String query1 = """
            SELECT e FROM EHR e CONTAINS COMPOSITION c11[openEHR-EHR-COMPOSITION.health_summary.v1] CONTAINS OBSERVATION o12[openEHR-EHR-OBSERVATION.body_weight.v2] \
            WHERE (c11/archetype_details/template_id/value = 'International Patient Summary' AND o12/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude = $Weight)""";
    String expected = """
            SELECT e/ehr_id/value FROM EHR e CONTAINS COMPOSITION aql1_c11[openEHR-EHR-COMPOSITION.health_summary.v1] CONTAINS OBSERVATION aql1_o12[openEHR-EHR-OBSERVATION.body_weight.v2] \
            WHERE (aql1_c11/archetype_details/template_id/value = 'International Patient Summary' AND aql1_o12/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude = $aql1_Weight)""";


    CohortAql cohortAql1 = CohortAql.builder().id(1L).name(AQL_NAME).query(query1).build();

    CohortGroup cohort =
            CohortGroup.builder().type(Type.AQL).query(cohortAql1).parameters(Map.of("Weight", 1)).build();

    var result = aqlCombiner.combineQuery(cohort);

    MatcherAssert.assertThat(
            result.aqlQuery.render(),
            Is.is(expected));
    MatcherAssert.assertThat(
            result.parameters,
            Is.is(Map.of("aql1_Weight", 1)));
  }

  @Test
  public void combineTwoQueryWithAnd() {
    String query1 = """
            SELECT e FROM EHR e CONTAINS COMPOSITION c01[openEHR-EHR-COMPOSITION.health_summary.v1] CONTAINS OBSERVATION o02[openEHR-EHR-OBSERVATION.blood_pressure.v2] \
            WHERE (c01/archetype_details/template_id/value = 'International Patient Summary' AND o02/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude > $Systolic AND \
            o02/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude < $Diastolic)""";
    String query2 = """
            SELECT e FROM EHR e CONTAINS COMPOSITION c11[openEHR-EHR-COMPOSITION.health_summary.v1] CONTAINS OBSERVATION o12[openEHR-EHR-OBSERVATION.body_weight.v2] \
            WHERE (c11/archetype_details/template_id/value = 'International Patient Summary' AND o12/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude = $Weight)""";
    String expected = """
            SELECT e/ehr_id/value FROM EHR e CONTAINS ((COMPOSITION aql1_c01[openEHR-EHR-COMPOSITION.health_summary.v1] CONTAINS OBSERVATION aql1_o02[openEHR-EHR-OBSERVATION.blood_pressure.v2]) AND \
            (COMPOSITION aql2_c11[openEHR-EHR-COMPOSITION.health_summary.v1] CONTAINS OBSERVATION aql2_o12[openEHR-EHR-OBSERVATION.body_weight.v2])) \
            WHERE ((aql1_c01/archetype_details/template_id/value = 'International Patient Summary' AND aql1_o02/data[at0001]/events[at0006]/data[at0003]/items[at0004]/value/magnitude > $aql1_Systolic AND \
            aql1_o02/data[at0001]/events[at0006]/data[at0003]/items[at0005]/value/magnitude < $aql1_Diastolic) AND \
            (aql2_c11/archetype_details/template_id/value = 'International Patient Summary' AND aql2_o12/data[at0002]/events[at0003]/data[at0001]/items[at0004]/value/magnitude = $aql2_Weight))""";


    CohortAql cohortAql1 = CohortAql.builder().id(1L).name(AQL_NAME).query(query1).build();
    CohortAql cohortAql2 = CohortAql.builder().id(2L).name(AQL_NAME).query(query2).build();

    CohortGroup first =
            CohortGroup.builder().type(Type.AQL).query(cohortAql1).parameters(Map.of("Systolic", 1, "Diastolic", 2)).build();
    CohortGroup second =
            CohortGroup.builder().type(Type.AQL).query(cohortAql2).parameters(Map.of("Weight", 3)).build();

    CohortGroup andCohort =
            CohortGroup.builder()
                    .type(Type.GROUP)
                    .operator(Operator.AND)
                    .children(List.of(first, second))
                    .build();

    var result = aqlCombiner.combineQuery(andCohort);

    MatcherAssert.assertThat(
            result.aqlQuery.render(),
            Is.is(expected));
    MatcherAssert.assertThat(
            result.parameters,
            Is.is(Map.of("aql1_Systolic", 1, "aql1_Diastolic", 2, "aql2_Weight", 3)));
  }
}
