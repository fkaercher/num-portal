package org.highmed.numportal.service;

import org.highmed.numportal.domain.dto.CohortDto;
import org.highmed.numportal.domain.dto.CohortGroupDto;
import org.highmed.numportal.domain.dto.CohortSizeDto;
import org.highmed.numportal.domain.dto.TemplateSizeRequestDto;
import org.highmed.numportal.domain.model.Cohort;
import org.highmed.numportal.domain.model.CohortGroup;
import org.highmed.numportal.domain.model.Project;
import org.highmed.numportal.domain.model.ProjectStatus;
import org.highmed.numportal.domain.model.Type;
import org.highmed.numportal.domain.repository.CohortRepository;
import org.highmed.numportal.domain.repository.ProjectRepository;
import org.highmed.numportal.properties.PrivacyProperties;
import org.highmed.numportal.service.ehrbase.EhrBaseService;
import org.highmed.numportal.service.exception.BadRequestException;
import org.highmed.numportal.service.exception.ForbiddenException;
import org.highmed.numportal.service.exception.PrivacyException;
import org.highmed.numportal.service.exception.ResourceNotFound;
import org.highmed.numportal.service.executors.CohortExecutor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.dto.condition.ComparisonOperatorCondition;
import org.ehrbase.openehr.sdk.aql.dto.condition.LogicalOperatorCondition;
import org.ehrbase.openehr.sdk.aql.dto.condition.WhereCondition;
import org.ehrbase.openehr.sdk.aql.dto.operand.Operand;
import org.ehrbase.openehr.sdk.aql.dto.operand.QueryParameter;
import org.ehrbase.openehr.sdk.aql.parser.AqlQueryParser;
import org.ehrbase.openehr.sdk.response.dto.QueryResponseData;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.highmed.numportal.domain.templates.ExceptionsTemplate.CHANGING_COHORT_ONLY_ALLOWED_BY_THE_OWNER_OF_THE_PROJECT;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.COHORT_CHANGE_ONLY_ALLOWED_ON_PROJECT_STATUS_DRAFT_OR_PENDING;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.COHORT_GROUP_CANNOT_BE_EMPTY;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.COHORT_NOT_FOUND;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.INVALID_AQL_ID;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.INVALID_COHORT_GROUP_AQL_MISSING;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.INVALID_COHORT_GROUP_AQL_MISSING_PARAMETERS;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.INVALID_COHORT_GROUP_CHILDREN_MISSING;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.PROJECT_NOT_FOUND;
import static org.highmed.numportal.domain.templates.ExceptionsTemplate.RESULTS_WITHHELD_FOR_PRIVACY_REASONS;

@Slf4j
@Service
@AllArgsConstructor
public class CohortService {

  public static final String TEMPLATE_PATH = "archetype_details/template_id/value";
  public static final String HOSPITAL_PATH = "context/health_care_facility/name";
  public static final String GET_PATIENTS_PER_AGE_INTERVAL =
      "SELECT count(e/ehr_id/value) "
          + "FROM EHR e contains OBSERVATION o0[openEHR-EHR-OBSERVATION.age.v0] "
          + "WHERE o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/value >= 'P%dY' "
          + "AND o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/value < 'P%dY'"
          + "AND e/ehr_id/value MATCHES {%s} ";
  private static final String AGE_INTERVAL_LABEL = "%d-%d";
  private static final int MAX_AGE = 122;
  private static final int AGE_INTERVAL = 10;
  private final CohortRepository cohortRepository;
  private final CohortExecutor cohortExecutor;
  private final UserDetailsService userDetailsService;
  private final ModelMapper modelMapper;
  private final AqlService aqlService;
  private final ProjectRepository projectRepository;
  private final PrivacyProperties privacyProperties;
  private final EhrBaseService ehrBaseService;

  public Cohort getCohort(Long cohortId, String userId) {
    userDetailsService.checkIsUserApproved(userId);
    return cohortRepository.findById(cohortId).orElseThrow(
        () -> new ResourceNotFound(CohortService.class, COHORT_NOT_FOUND, String.format(COHORT_NOT_FOUND, cohortId)));
  }

  public Cohort createCohort(CohortDto cohortDto, String userId) {
    userDetailsService.checkIsUserApproved(userId);

    Project project =
        projectRepository
            .findById(cohortDto.getProjectId())
            .orElseThrow(
                () -> new ResourceNotFound(ProjectService.class, PROJECT_NOT_FOUND, String.format(PROJECT_NOT_FOUND, cohortDto.getProjectId())));

    checkProjectModifiable(project, userId);

    Cohort cohort =
        Cohort.builder()
              .name(cohortDto.getName())
              .description(cohortDto.getDescription())
              .project(project)
              .cohortGroup(convertToCohortGroupEntity(cohortDto.getCohortGroup()))
              .build();

    project.setCohort(cohort);
    log.info("Cohort created by user {}", userId);
    return cohortRepository.save(cohort);
  }

  public Cohort toCohort(CohortDto cohortDto) {
    return Cohort.builder()
                 .name(cohortDto.getName())
                 .description(cohortDto.getDescription())
                 .cohortGroup(convertToCohortGroupEntity(cohortDto.getCohortGroup()))
                 .build();
  }

  public Set<String> executeCohort(long cohortId, Boolean allowUsageOutsideEu) {
    Optional<Cohort> cohort = cohortRepository.findById(cohortId);
    return cohortExecutor.execute(
        cohort.orElseThrow(() -> new BadRequestException(CohortService.class, COHORT_NOT_FOUND, String.format(COHORT_NOT_FOUND, cohortId))),
        allowUsageOutsideEu);
  }

  public Set<String> executeCohort(Cohort cohort, Boolean allowUsageOutsideEu) {
    return cohortExecutor.execute(cohort, allowUsageOutsideEu);
  }

  public long getCohortGroupSize(CohortGroupDto cohortGroupDto, String userId, Boolean allowUsageOutsideEu) {
    userDetailsService.checkIsUserApproved(userId);
    CohortGroup cohortGroup = convertToCohortGroupEntity(cohortGroupDto);
    return getCohortGroupSizeAndCheck(cohortGroup, allowUsageOutsideEu);
  }

  public int getRoundedSize(long size) {
    return Math.round((float) size / 10) * 10;
  }

  public Map<String, Integer> getSizePerTemplates(
      String userId, TemplateSizeRequestDto requestDto) {
    userDetailsService.checkIsUserApproved(userId);

    CohortGroup cohortGroup = convertToCohortGroupEntity(requestDto.getCohortDto().getCohortGroup());
    getCohortGroupSizeAndCheck(cohortGroup, false);

    Map<String, Integer> templateMap = requestDto.getTemplateIds().stream().collect(Collectors.toMap(Function.identity(), e -> 0));
    templateMap.putAll(cohortExecutor.executeNumberOfPatientsPerPath(cohortGroup, false, TEMPLATE_PATH, templateMap.keySet().stream().toList()));
    return templateMap;
  }

  public Cohort updateCohort(CohortDto cohortDto, Long cohortId, String userId) {
    userDetailsService.checkIsUserApproved(userId);

    Cohort cohortToEdit =
        cohortRepository
            .findById(cohortId)
            .orElseThrow(() -> new ResourceNotFound(CohortService.class, COHORT_NOT_FOUND, String.format(COHORT_NOT_FOUND, cohortId)));

    Project project = cohortToEdit.getProject();

    checkProjectModifiable(project, userId);

    cohortToEdit.setCohortGroup(convertToCohortGroupEntity(cohortDto.getCohortGroup()));
    cohortToEdit.setDescription(cohortDto.getDescription());
    cohortToEdit.setName(cohortDto.getName());
    log.info("User {} updated cohort {}", userId, cohortId);
    return cohortRepository.save(cohortToEdit);
  }

  private void checkProjectModifiable(Project project, String userId) {
    if (project.hasEmptyOrDifferentOwner(userId)) {
      throw new ForbiddenException(AqlService.class, CHANGING_COHORT_ONLY_ALLOWED_BY_THE_OWNER_OF_THE_PROJECT);
    }

    if (project.getStatus() != ProjectStatus.DRAFT
        && project.getStatus() != ProjectStatus.PENDING
        && project.getStatus() != ProjectStatus.CHANGE_REQUEST) {
      throw new ForbiddenException(AqlService.class, COHORT_CHANGE_ONLY_ALLOWED_ON_PROJECT_STATUS_DRAFT_OR_PENDING);
    }
  }

  private void validateCohortParameters(CohortGroup cohortGroupDto) {
    if (cohortGroupDto.getType() == Type.GROUP && CollectionUtils.isEmpty(cohortGroupDto.getChildren())) {
      throw new BadRequestException(CohortService.class, INVALID_COHORT_GROUP_CHILDREN_MISSING);
    }
    if (cohortGroupDto.getType() == Type.AQL) {
      if (Objects.isNull(cohortGroupDto.getQuery())) {
        throw new BadRequestException(CohortGroup.class, INVALID_COHORT_GROUP_AQL_MISSING);
      }
      Set<String> parameterNames = new HashSet<>();
      AqlQuery aqlDto = AqlQueryParser.parse(cohortGroupDto.getQuery().getQuery());
      WhereCondition conditionDto = aqlDto.getWhere();
      if (conditionDto instanceof ComparisonOperatorCondition) {
        Operand value = ((ComparisonOperatorCondition) conditionDto).getValue();
        if (value instanceof QueryParameter parameterValue) {
          parameterNames.add(parameterValue.getName());
        }
      } else if (conditionDto instanceof LogicalOperatorCondition) {
        List<WhereCondition> values = ((LogicalOperatorCondition) conditionDto).getValues();
        for (WhereCondition v : values) {
          if (v instanceof ComparisonOperatorCondition) {
            Operand value = ((ComparisonOperatorCondition) v).getValue();
            if (value instanceof QueryParameter parameterValue) {
              parameterNames.add(parameterValue.getName());
            }
          }
        }
      }
      if (CollectionUtils.isNotEmpty(parameterNames) && MapUtils.isEmpty(cohortGroupDto.getParameters())) {
        log.error("The query is invalid. The value of parameter(s) {} is missing", parameterNames);
        throw new BadRequestException(CohortService.class, INVALID_COHORT_GROUP_AQL_MISSING_PARAMETERS);
      } else if (CollectionUtils.isNotEmpty(parameterNames) && MapUtils.isNotEmpty(cohortGroupDto.getParameters())) {
        Set<String> receivedParams = cohortGroupDto.getParameters().keySet();
        if (!receivedParams.containsAll(parameterNames)) {
          parameterNames.removeAll(receivedParams);
          log.error("The query is invalid. The value of parameter {} is missing", parameterNames);
          throw new BadRequestException(CohortService.class, INVALID_COHORT_GROUP_AQL_MISSING_PARAMETERS);
        }
      }
    }
    if (CollectionUtils.isNotEmpty(cohortGroupDto.getChildren())) {
      cohortGroupDto.getChildren()
                    .forEach(this::validateCohortParameters);
    }
  }

  private CohortGroup convertToCohortGroupEntity(CohortGroupDto cohortGroupDto) {
    if (cohortGroupDto == null) {
      throw new BadRequestException(CohortGroup.class, COHORT_GROUP_CANNOT_BE_EMPTY);
    }

    CohortGroup cohortGroup = modelMapper.map(cohortGroupDto, CohortGroup.class);
    cohortGroup.setId(null);

    if (cohortGroupDto.isAql()) {
      if (cohortGroupDto.getQuery() != null && cohortGroupDto.getQuery().getId() != null) {
        if (!aqlService.existsById(cohortGroupDto.getQuery().getId())) {
          throw new BadRequestException(CohortGroup.class, INVALID_AQL_ID,
              String.format("%s: %s", INVALID_AQL_ID, cohortGroupDto.getQuery().getId()));
        }
      } else {
        throw new BadRequestException(CohortGroup.class, INVALID_COHORT_GROUP_AQL_MISSING);
      }
    }

    if (cohortGroupDto.isGroup()) {
      if (CollectionUtils.isNotEmpty(cohortGroup.getChildren())) {
        cohortGroup.setChildren(
            cohortGroupDto.getChildren().stream()
                          .map(
                              child -> {
                                CohortGroup cohortGroupChild = convertToCohortGroupEntity(child);
                                cohortGroupChild.setParent(cohortGroup);
                                return cohortGroupChild;
                              })
                          .collect(Collectors.toList()));
      } else {
        throw new BadRequestException(CohortService.class, INVALID_COHORT_GROUP_CHILDREN_MISSING);
      }
    }
    return cohortGroup;
  }

  public CohortSizeDto getCohortGroupSizeWithDistribution(CohortGroupDto cohortGroupDto, String userId, Boolean allowUsageOutsideEu) {
    userDetailsService.checkIsUserApproved(userId);
    CohortGroup cohortGroup = convertToCohortGroupEntity(cohortGroupDto);

    long count = getCohortGroupSizeAndCheck(cohortGroup, allowUsageOutsideEu);
    if (count == 0) {
      return CohortSizeDto.builder().build();
    }

    var hospitals = getSizesPerHospital(cohortGroup, allowUsageOutsideEu);
    var ageGroups = getSizesPerAgeGroup(cohortGroup, allowUsageOutsideEu);
    return CohortSizeDto.builder().hospitals(hospitals).ages(ageGroups).count((int) count).build();
  }

  private long getCohortGroupSizeAndCheck(CohortGroup cohortGroup, Boolean allowUsageOutsideEu) {
    validateCohortParameters(cohortGroup);
    long ehrIds = cohortExecutor.executeNumberOfPatients(cohortGroup, allowUsageOutsideEu);
    if (ehrIds < privacyProperties.getMinHits()) {
      log.warn(RESULTS_WITHHELD_FOR_PRIVACY_REASONS);
      throw new PrivacyException(CohortService.class, RESULTS_WITHHELD_FOR_PRIVACY_REASONS);
    }
    return ehrIds;
  }

  private Map<String, Integer> getSizesPerAgeGroup(CohortGroup cohortGroup, Boolean allowUsageOutsideEu) {
    Set<String> ehrIds = cohortExecutor.executePatientIds(cohortGroup, allowUsageOutsideEu);
    String idsString = "'" + String.join("','", ehrIds) + "'";
    Map<String, Integer> sizes = new LinkedHashMap<>();
    for (int age = 0; age < MAX_AGE; age += AGE_INTERVAL) {
      QueryResponseData queryResponseData =
          ehrBaseService.executePlainQuery(
              String.format(GET_PATIENTS_PER_AGE_INTERVAL, age, age + AGE_INTERVAL, idsString));
      List<List<Object>> rows = queryResponseData.getRows();
      String range = String.format(AGE_INTERVAL_LABEL, age, age + AGE_INTERVAL);
      if (rows == null || rows.get(0) == null || rows.get(0).get(0) == null) {
        sizes.put(range, 0);
      } else {
        sizes.put(range, (Integer) rows.get(0).get(0));
      }
    }
    return sizes;
  }

  private Map<String, Integer> getSizesPerHospital(CohortGroup cohortGroup, Boolean allowUsageOutsideEu) {
    return cohortExecutor.executeNumberOfPatientsPerPath(cohortGroup, allowUsageOutsideEu, HOSPITAL_PATH, List.of());
  }
}
