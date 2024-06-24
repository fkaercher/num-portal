package org.highmed.aqleditor.controller;

import lombok.AllArgsConstructor;
import org.highmed.aqleditor.dto.containment.ContainmentDto;
import org.highmed.aqleditor.service.AqlEditorContainmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/aqleditor/v1/containment",
    produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class AqlEditorContainmentController extends BaseController {

  private final AqlEditorContainmentService aqlEditorContainmentService;

  @GetMapping(path = "{templateId}")
  public ResponseEntity<ContainmentDto> getByTEmplateId(
      @PathVariable(value = "templateId") String templateId) {
    return ResponseEntity.ok(aqlEditorContainmentService.buildContainment(templateId));
  }
}
