package org.highmed.numportal.service.executors;

import org.highmed.numportal.properties.ConsentProperties;
import org.highmed.numportal.service.policy.ProjectPolicyService;

import org.ehrbase.openehr.sdk.aql.parser.AqlQueryParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class AqlExecutorTest {

    @Mock
    private ProjectPolicyService projectPolicyService;

    @Mock
    private ConsentProperties consentProperties;

    @InjectMocks
    private AqlExecutor aqlExecutor;

    private final String Q1 = "SELECT c0 AS test FROM EHR e CONTAINS COMPOSITION c0[openEHR-EHR-COMPOSITION.report.v1]";

    @Test
    public void shouldPrepareQueryCohortAql() {
        AqlWithParams aql = new AqlWithParams(
                AqlQueryParser.parse(Q1),
                Collections.emptyMap()
        );
        var result = aqlExecutor.prepareQuery(aql, false);
        assertThat(result, is(Q1));
    }

    @Test
    public void shouldPrepareQueryCohortAqlOutsideEU() {
        AqlWithParams aql = new AqlWithParams(
                AqlQueryParser.parse(Q1),
                Collections.emptyMap()
        );
        var result = aqlExecutor.prepareQuery(aql, true);
        assertThat(result, is(Q1));
    }

    @Test
    public void shouldPrepareQueryCohortAqlWithParams() {
        final String Q2_PARAMS = "SELECT c0 AS GECCO_Personendaten " +
                "FROM EHR e CONTAINS COMPOSITION c0[openEHR-EHR-COMPOSITION.registereintrag.v1] CONTAINS CLUSTER c1[openEHR-EHR-CLUSTER.person_birth_data_iso.v0] " +
                "WHERE (c0/archetype_details/template_id/value = 'GECCO_Personendaten' AND c1/items[at0001]/value/value = $Geburtsdatum)";
        AqlWithParams aql = new AqlWithParams(
                AqlQueryParser.parse(Q2_PARAMS),
                Map.of("Geburstdatum", "1985-05-12")
        );
        var result = aqlExecutor.prepareQuery(aql, false);
        assertThat(result, is(Q2_PARAMS));
    }
}
