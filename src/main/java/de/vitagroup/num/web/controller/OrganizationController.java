package de.vitagroup.num.web.controller;

import de.vitagroup.num.domain.dto.OrganizationDto;
import de.vitagroup.num.service.OrganizationService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@AllArgsConstructor
public class OrganizationController {

  private final OrganizationService organizationService;

  @GetMapping("/organization/{id}")
  @ApiOperation(value = "Retrieves an organization by external id")
  public ResponseEntity<OrganizationDto> getOrganizationById(
      @NotNull @NotEmpty @PathVariable String id) {
    return ResponseEntity.ok(organizationService.getOrganizationById(id));
  }

  @GetMapping("/organization")
  @ApiOperation(value = "Retrieves a list of available organizations")
  public ResponseEntity<List<OrganizationDto>> getAllOrganizations() {
    return ResponseEntity.ok(organizationService.getAllOrganizations());
  }
}
