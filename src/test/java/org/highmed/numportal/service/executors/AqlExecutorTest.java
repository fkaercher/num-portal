package org.highmed.numportal.service.executors;

import org.highmed.numportal.properties.ConsentProperties;
import org.highmed.numportal.service.ehrbase.EhrBaseService;
import org.highmed.numportal.service.policy.ProjectPolicyService;

import org.ehrbase.openehr.sdk.aql.parser.AqlQueryParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AqlExecutorTest {

    @Mock
    private EhrBaseService ehrBaseService;

    @Mock
    private ProjectPolicyService projectPolicyService;

    @Mock
    private ConsentProperties consentProperties;

    @InjectMocks
    private AqlExecutor aqlExecutor;

    private final String Q1 = "SELECT c0 AS test FROM EHR e CONTAINS COMPOSITION c0[openEHR-EHR-COMPOSITION.report.v1]";

    @Before
    public void setup() {
        when(ehrBaseService.retrieveEligiblePatientIds(Mockito.any(String.class))).thenReturn(Set.of("id1", "id2", "id3"));
    }
    @Test
    public void shouldExecuteCohortAql() {
        AqlWithParams aql = new AqlWithParams(
                AqlQueryParser.parse(Q1),
                Collections.emptyMap()
        );
        aqlExecutor.execute(aql, false);
        Mockito.verify(ehrBaseService, Mockito.times(1)).retrieveEligiblePatientIds(Q1);
    }

    @Test
    public void shouldExecuteCohortAqlOutsideEU() {
        AqlWithParams aql = new AqlWithParams(
                AqlQueryParser.parse(Q1),
                Collections.emptyMap()
        );
        aqlExecutor.execute(aql, true);
        Mockito.verify(ehrBaseService, Mockito.times(1)).retrieveEligiblePatientIds(Q1);
    }

    @Test
    public void shouldExecuteCohortAqlWithParams() {
        final String Q2_PARAMS = "SELECT c0 AS GECCO_Personendaten " +
                "FROM EHR e CONTAINS COMPOSITION c0[openEHR-EHR-COMPOSITION.registereintrag.v1] CONTAINS CLUSTER c1[openEHR-EHR-CLUSTER.person_birth_data_iso.v0] " +
                "WHERE (c0/archetype_details/template_id/value = 'GECCO_Personendaten' AND c1/items[at0001]/value/value = $Geburtsdatum)";
        AqlWithParams aql = new AqlWithParams(
                AqlQueryParser.parse(Q2_PARAMS),
                Map.of("Geburstdatum", "1985-05-12")
        );
        aqlExecutor.execute(aql, false);
        Mockito.verify(ehrBaseService, Mockito.times(1)).retrieveEligiblePatientIds(Q2_PARAMS);
    }
}
