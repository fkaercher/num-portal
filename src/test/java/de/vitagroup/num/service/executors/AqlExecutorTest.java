package de.vitagroup.num.service.executors;

import de.vitagroup.num.domain.CohortAql;
import de.vitagroup.num.properties.ConsentProperties;
import de.vitagroup.num.service.ehrbase.EhrBaseService;
import de.vitagroup.num.service.policy.ProjectPolicyService;
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

    private final String Q1 = "Select c0 as test from EHR e contains COMPOSITION c0[openEHR-EHR-COMPOSITION.report.v1]";
    private final String Q2_PARAMS = "SELECT  c0 as GECCO_Personendaten " +
            "FROM EHR e contains COMPOSITION c0[openEHR-EHR-COMPOSITION.registereintrag.v1] contains CLUSTER c1[openEHR-EHR-CLUSTER.person_birth_data_iso.v0] " +
            "WHERE (c0/archetype_details/template_id/value = 'GECCO_Personendaten' and c1/items[at0001]/value/value = $Geburtsdatum)";

    @Before
    public void setup() {
        when(ehrBaseService.retrieveEligiblePatientIds(Mockito.any(String.class))).thenReturn(Set.of("id1", "id2", "id3"));
    }
    @Test
    public void shouldExecuteCohortAql() {
        CohortAql aql = CohortAql.builder()
                .name("test query")
                .query(Q1)
                .build();
        aqlExecutor.execute(aql, Collections.emptyMap(), false);
        Mockito.verify(ehrBaseService, Mockito.times(1)).retrieveEligiblePatientIds(Q1);
    }

    @Test
    public void shouldExecuteCohortAqlOutsideEU() {
        CohortAql aql = CohortAql.builder()
                .name("test query")
                .query(Q1)
                .build();
        aqlExecutor.execute(aql, Collections.emptyMap(), true);
        Mockito.verify(ehrBaseService, Mockito.times(1)).retrieveEligiblePatientIds(Q1);
    }

    @Test
    public void shouldExecuteCohortAqlWithParams() {
        CohortAql aql = CohortAql.builder()
                .name("test query")
                .query(Q2_PARAMS)
                .build();
        aqlExecutor.execute(aql, Map.of("Geburstdatum", "1985-05-12"), false);
        Mockito.verify(ehrBaseService, Mockito.times(1)).retrieveEligiblePatientIds(Q2_PARAMS);
    }
}
