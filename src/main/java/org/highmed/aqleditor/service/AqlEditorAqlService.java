package org.highmed.aqleditor.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.highmed.aqleditor.dto.aql.QueryValidationResponse;
import org.highmed.aqleditor.dto.aql.Result;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.parser.AqlParseException;
import org.ehrbase.openehr.sdk.aql.parser.AqlQueryParser;
import org.ehrbase.openehr.sdk.aql.render.AqlRenderer;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class AqlEditorAqlService {

  public Result buildAql(AqlQuery aqlDto) {
    return new Result(AqlRenderer.render(aqlDto), null);
  }

  public AqlQuery parseAql(Result result) {
    return AqlQueryParser.parse(result.getQ());
  }

  public QueryValidationResponse validateAql(Result query) {
    try {
        AqlQueryParser.parse(query.getQ());
    } catch (AqlParseException e) {
      return buildResponse(e.getMessage());
    }

    return QueryValidationResponse.builder().valid(true).message("Query is valid").build();
  }

  public QueryValidationResponse buildResponse(String errorMessage) {
    if (StringUtils.isEmpty(errorMessage)) {
      return QueryValidationResponse.builder().valid(false).build();
    }

    Pattern pattern = Pattern.compile("^.*line (\\d+): char (\\d+) (.*).*$");
    Matcher matcher = pattern.matcher(errorMessage);

    if (matcher.matches()) {
      String line = matcher.group(1);
      String column = matcher.group(2);
      String error = matcher.group(3);
      return QueryValidationResponse.builder().valid(false).error(error).message(errorMessage)
          .startColumn(column).startLine(line).build();
    }

    return QueryValidationResponse.builder().valid(false).message(errorMessage).build();
  }
}
