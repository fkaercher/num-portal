package org.highmed.aqleditor.controller;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import org.highmed.aqleditor.dto.aql.QueryValidationResponse;
import org.highmed.aqleditor.dto.aql.Result;
import org.highmed.aqleditor.service.AqlEditorAqlService;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/aqleditor/v1/aql",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class AqlEditorAqlController extends BaseController {

  private AqlEditorAqlService aqlEditorAqlService;

  @PostMapping
  public ResponseEntity<Result> buildAql(@RequestBody AqlQuery aqlDto) {
    return ResponseEntity.ok(aqlEditorAqlService.buildAql(aqlDto));
  }

  @GetMapping
  public ResponseEntity<AqlQuery> parseAql(@RequestBody Result result) {
    return ResponseEntity.ok(aqlEditorAqlService.parseAql(result));
  }

  @PostMapping("/validate")
  public ResponseEntity<QueryValidationResponse> validateAql(@RequestBody @NotNull Result query) {
    return ResponseEntity.ok(aqlEditorAqlService.validateAql(query));
  }
}
