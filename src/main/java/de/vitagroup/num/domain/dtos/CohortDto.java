package de.vitagroup.num.domain.dtos;

import de.vitagroup.num.domain.validation.ValidCohort;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CohortDto {

    @ApiModelProperty(
            value = "The unique identifier",
            example = "1")
    private Long id;

    @ApiModelProperty(
            required = true,
            value = "The name of the cohort")
    @NotBlank(message = "Name should not be blank")
    @NotNull(message = "Name is mandatory")
    private String name;

    @ApiModelProperty(
            value = "The description of the cohort")
    private String description;

    @ApiModelProperty(
            required = true,
            value = "Reference to the study")
    @NotNull(message = "Id of the study is mandatory")
    private Long studyId;

    @ApiModelProperty(
            required = true,
            value = "Cohort phenotypes groups")
    @NotNull(message = "Cohort phenotype group mandatory")
    @ValidCohort(message = "Invalid cohort group")
    private CohortGroupDto cohortGroupDto;

}
