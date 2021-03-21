package de.vitagroup.num.web.controller;

import de.vitagroup.num.domain.dto.TemplateMetadataDto;
import de.vitagroup.num.service.TemplateService;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@AllArgsConstructor
@RequestMapping("/template")
public class TemplateController {

  private final TemplateService templateService;

  @GetMapping("/metadata")
  @ApiOperation(value = "Retrieves a list of template metadata")
  public ResponseEntity<List<TemplateMetadataDto>> getAllTemplatesMetadata(
      @AuthenticationPrincipal @NotNull Jwt principal) {
    return ResponseEntity.ok(templateService.getAllTemplatesMetadata(principal.getSubject()));
  }
}
