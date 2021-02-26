package de.vitagroup.num.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/** Dto for template metadata retrieved from ehr base */
@Data
@Builder
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMetadataDto {

  @ApiModelProperty(value = "The ehrbase template id")
  private String templateId;

  @ApiModelProperty private String name;

  @ApiModelProperty private String archetypeId;

  @ApiModelProperty private OffsetDateTime createdOn;
}
