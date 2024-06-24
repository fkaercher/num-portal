package org.highmed.aqleditor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import org.apache.commons.lang3.StringUtils;
import org.highmed.aqleditor.dto.aql.QueryValidationResponse;
import org.highmed.aqleditor.dto.aql.Result;
import org.highmed.aqleditor.service.AqlEditorAqlService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AqlServiceTest {

  @InjectMocks
  private AqlEditorAqlService aqlService;

  private final String INVALID_QUERY = "Select TOP 13 FORWARD  where (o0/data[at0001]/events[at0at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude < 1.1)";
  private final String VALID_QUERY = "Select o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude AS Systolic__magnitude, e/ehr_id/value AS ehr_id FROM EHR e CONTAINS OBSERVATION o0[openEHR-EHR-OBSERVATION.sample_blood_pressure.v1] WHERE (o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude >= $magnitude AND o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude < 1.1) LIMIT 13";

  @Test
  public void shouldCorrectlyValidateAql1() {
    Result aql = Result.builder().q("invalid aql").build();
    QueryValidationResponse response = aqlService.validateAql(aql);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), is("0"));
    assertThat(response.getStartLine(), is("1"));
    assertThat(response.getMessage(),
        containsStringIgnoringCase("Parse exception: line 1: char 0 missing SELECT at 'invalid'"));
    assertThat(response.getError(),
        containsStringIgnoringCase("missing SELECT at 'invalid'"));
  }

  @Test
  public void shouldCorrectlyValidateAql2() {

    Result aql = Result.builder().q(INVALID_QUERY).build();
    QueryValidationResponse response = aqlService.validateAql(aql);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), is("23"));
    assertThat(response.getStartLine(), is("1"));
    assertThat(response.getMessage(),
            containsStringIgnoringCase("Parse exception: line 1: char 23 mismatched input 'where' expecting {NULL, CONTAINS, STRING_FUNCTION_ID, NUMERIC_FUNCTION_ID, DATE_TIME_FUNCTION_ID, COUNT, MIN, MAX, SUM, AVG, TERMINOLOGY, BOOLEAN, IDENTIFIER, INTEGER, REAL, SCI_INTEGER, SCI_REAL, DATE, TIME, DATETIME, STRING, '-'}"));
    assertThat(response.getError(),
            containsStringIgnoringCase("mismatched input 'where' expecting {NULL, CONTAINS, STRING_FUNCTION_ID, NUMERIC_FUNCTION_ID, DATE_TIME_FUNCTION_ID, COUNT, MIN, MAX, SUM, AVG, TERMINOLOGY, BOOLEAN, IDENTIFIER, INTEGER, REAL, SCI_INTEGER, SCI_REAL, DATE, TIME, DATETIME, STRING, '-'}"));
  }

  @Test
  public void shouldCorrectlyValidateAql3() {
    Result aql = Result.builder().q(VALID_QUERY).build();
    QueryValidationResponse response = aqlService.validateAql(aql);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(true));
    assertThat(response.getMessage(), containsStringIgnoringCase("Query is valid"));
  }

  @Test
  public void shouldCorrectlyBuildErrorResponseWhenLineAndCharMissing() {

    QueryValidationResponse response = aqlService.buildResponse("AQL Parse exception:");

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), nullValue());
    assertThat(response.getStartLine(), nullValue());
    assertThat(response.getMessage(),
        containsStringIgnoringCase("AQL Parse exception:"));
  }

  @Test
  public void shouldCorrectlyBuildErrorResponseWhenEmpty() {

    QueryValidationResponse response = aqlService.buildResponse(StringUtils.EMPTY);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), nullValue());
    assertThat(response.getStartLine(), nullValue());
    assertThat(response.getMessage(), nullValue());
  }

  @Test
  public void shouldCorrectlyBuildErrorResponseWhenNull() {

    QueryValidationResponse response = aqlService.buildResponse(null);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), nullValue());
    assertThat(response.getStartLine(), nullValue());
    assertThat(response.getMessage(), nullValue());
  }
}
