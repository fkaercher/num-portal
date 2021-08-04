package de.vitagroup.num.service;

import de.vitagroup.num.domain.Cohort;
import de.vitagroup.num.domain.CohortGroup;
import de.vitagroup.num.domain.Project;
import de.vitagroup.num.domain.ProjectStatus;
import de.vitagroup.num.domain.dto.CohortDto;
import de.vitagroup.num.domain.dto.CohortGroupDto;
import de.vitagroup.num.domain.dto.CohortSizeDto;
import de.vitagroup.num.domain.dto.TemplateSizeRequestDto;
import de.vitagroup.num.domain.repository.CohortRepository;
import de.vitagroup.num.domain.repository.ProjectRepository;
import de.vitagroup.num.properties.PrivacyProperties;
import de.vitagroup.num.service.ehrbase.EhrBaseService;
import de.vitagroup.num.service.executors.CohortExecutor;
import de.vitagroup.num.service.policy.EhrPolicy;
import de.vitagroup.num.service.policy.Policy;
import de.vitagroup.num.service.policy.ProjectPolicyService;
import de.vitagroup.num.service.policy.TemplatesPolicy;
import de.vitagroup.num.web.exception.BadRequestException;
import de.vitagroup.num.web.exception.ForbiddenException;
import de.vitagroup.num.web.exception.PrivacyException;
import de.vitagroup.num.web.exception.ResourceNotFound;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ehrbase.aql.binder.AqlBinder;
import org.ehrbase.aql.dto.AqlDto;
import org.ehrbase.response.openehr.QueryResponseData;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class CohortService {

  private final CohortRepository cohortRepository;
  private final CohortExecutor cohortExecutor;
  private final UserDetailsService userDetailsService;
  private final ModelMapper modelMapper;
  private final AqlService aqlService;
  private final ProjectRepository projectRepository;
  private final PrivacyProperties privacyProperties;
  private final ProjectPolicyService policyService;
  private final EhrBaseService ehrBaseService;
  private final ContentService contentService;
  private final TemplateService templateService;

  private static final String GET_PATIENTS_PER_CLINIC =
      "SELECT e/ehr_id/value as patient_id "
          + "FROM EHR e CONTAINS COMPOSITION c "
          + "WHERE c/context/health_care_facility/name = '%s'"
          + "AND e/ehr_id/value MATCHES {%s} ";
  private static final String GET_PATIENTS_PER_AGE_INTERVAL =
      "SELECT count(e/ehr_id/value) "
          + "FROM EHR e contains OBSERVATION o0[openEHR-EHR-OBSERVATION.age.v0] "
          + "WHERE o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/value >= 'P%dY' "
          + "AND o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/value < 'P%dY'"
          + "AND e/ehr_id/value MATCHES {%s} ";
  private static final String AGE_INTERVAL_LABEL = "%d-%d";
  private static final int MAX_AGE = 122;
  private static final int AGE_INTERVAL = 10;

  public Cohort getCohort(Long cohortId, String userId) {
    userDetailsService.checkIsUserApproved(userId);
    return cohortRepository.findById(cohortId).orElseThrow(ResourceNotFound::new);
  }

  public Cohort createCohort(CohortDto cohortDto, String userId) {
    userDetailsService.checkIsUserApproved(userId);

    Project project =
        projectRepository
            .findById(cohortDto.getProjectId())
            .orElseThrow(
                () -> new ResourceNotFound("Project not found: " + cohortDto.getProjectId()));

    checkProjectModifiable(project, userId);

    Cohort cohort =
        Cohort.builder()
            .name(cohortDto.getName())
            .description(cohortDto.getDescription())
            .project(project)
            .cohortGroup(convertToCohortGroupEntity(cohortDto.getCohortGroup()))
            .build();

    project.setCohort(cohort);
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
        cohort.orElseThrow(() -> new BadRequestException("Cohort not found: " + cohortId)),
        allowUsageOutsideEu);
  }

  public Set<String> executeCohort(Cohort cohort, Boolean allowUsageOutsideEu) {
    return cohortExecutor.execute(cohort, allowUsageOutsideEu);
  }

  public long getCohortGroupSize(
      CohortGroupDto cohortGroupDto, String userId, Boolean allowUsageOutsideEu) {
    userDetailsService.checkIsUserApproved(userId);

    CohortGroup cohortGroup = convertToCohortGroupEntity(cohortGroupDto);
    Set<String> ehrIds = cohortExecutor.executeGroup(cohortGroup, allowUsageOutsideEu);
    if (ehrIds.size() < privacyProperties.getMinHits()) {
      throw new PrivacyException(PrivacyProperties.RESULTS_WITHHELD_FOR_PRIVACY_REASONS);
    }
    return ehrIds.size();
  }

  public int getRoundedSize(long size) {
    return Math.round((float) size / 10) * 10;
  }

  public Map<String, Integer> getSizePerTemplates(
      String userId, TemplateSizeRequestDto requestDto) {
    userDetailsService.checkIsUserApproved(userId);

    Cohort cohort =
        Cohort.builder()
            .cohortGroup(convertToCohortGroupEntity(requestDto.getCohortDto().getCohortGroup()))
            .build();

    Set<String> ehrIds = cohortExecutor.execute(cohort, false);
    if (ehrIds.size() < privacyProperties.getMinHits()) {
      throw new PrivacyException(PrivacyProperties.RESULTS_WITHHELD_FOR_PRIVACY_REASONS);
    }

    return determineTemplatesHits(ehrIds, requestDto.getTemplateIds());
  }

  private Map<String, Integer> determineTemplatesHits(
      Set<String> ehrIds, List<String> templateIds) {
    Map<String, Integer> hits = new HashMap<>();
    templateIds.forEach(templateId -> getTemplateHits(ehrIds, hits, templateId));
    return hits;
  }

  private void getTemplateHits(Set<String> ehrIds, Map<String, Integer> hits, String templateId) {
    try {
      AqlDto aql = templateService.createSelectCompositionQuery(templateId);

      List<Policy> policies = new LinkedList<>();
      policies.add(EhrPolicy.builder().cohortEhrIds(ehrIds).build());
      policies.add(TemplatesPolicy.builder().templatesMap(Map.of(templateId, templateId)).build());
      policyService.apply(aql, policies);

      Set<String> templateHits =
          ehrBaseService.retrieveEligiblePatientIds(new AqlBinder().bind(aql).getLeft().buildAql());
      hits.put(templateId, templateHits != null ? templateHits.size() : 0);

    } catch (Exception e) {
      log.error(e.getMessage(), e);

      if (StringUtils.isNotEmpty(templateId)) {
        hits.put(templateId, -1);
      }
    }
  }

  public Cohort updateCohort(CohortDto cohortDto, Long cohortId, String userId) {
    userDetailsService.checkIsUserApproved(userId);

    Cohort cohortToEdit =
        cohortRepository
            .findById(cohortId)
            .orElseThrow(() -> new ResourceNotFound("Cohort not found: " + cohortId));

    Project project = cohortToEdit.getProject();

    checkProjectModifiable(project, userId);

    cohortToEdit.setCohortGroup(convertToCohortGroupEntity(cohortDto.getCohortGroup()));
    cohortToEdit.setDescription(cohortDto.getDescription());
    cohortToEdit.setName(cohortDto.getName());
    return cohortRepository.save(cohortToEdit);
  }

  private void checkProjectModifiable(Project project, String userId) {
    if (project.hasEmptyOrDifferentOwner(userId)) {
      throw new ForbiddenException("Changing cohort only allowed by the owner of the project");
    }

    if (project.getStatus() != ProjectStatus.DRAFT
        && project.getStatus() != ProjectStatus.PENDING
        && project.getStatus() != ProjectStatus.CHANGE_REQUEST) {
      throw new ForbiddenException("Cohort change only allowed on project status draft or pending");
    }
  }

  private CohortGroup convertToCohortGroupEntity(CohortGroupDto cohortGroupDto) {
    if (cohortGroupDto == null) {
      throw new BadRequestException("Cohort group cannot be empty");
    }

    CohortGroup cohortGroup = modelMapper.map(cohortGroupDto, CohortGroup.class);
    cohortGroup.setId(null);

    if (cohortGroupDto.isAql()) {
      if (cohortGroupDto.getQuery() != null && cohortGroupDto.getQuery().getId() != null) {

        if (!aqlService.existsById(cohortGroupDto.getQuery().getId())) {
          throw new BadRequestException(
              String.format("%s %s", "Invalid aql id:", cohortGroupDto.getQuery().getId()));
        }

      } else {
        throw new BadRequestException("Invalid cohort group. Aql missing.");
      }
    }

    if (cohortGroupDto.isGroup()) {
      cohortGroup.setChildren(
          cohortGroupDto.getChildren().stream()
              .map(
                  child -> {
                    CohortGroup cohortGroupChild = convertToCohortGroupEntity(child);
                    cohortGroupChild.setParent(cohortGroup);
                    return cohortGroupChild;
                  })
              .collect(Collectors.toList()));
    }

    return cohortGroup;
  }

  public CohortSizeDto getCohortGroupSizeWithDistribution(
      CohortGroupDto cohortGroupDto, String userId, Boolean allowUsageOutsideEu) {
    userDetailsService.checkIsUserApproved(userId);

    CohortGroup cohortGroup = convertToCohortGroupEntity(cohortGroupDto);
    Set<String> ehrIds = cohortExecutor.executeGroup(cohortGroup, allowUsageOutsideEu);
    if (ehrIds.size() < privacyProperties.getMinHits()) {
      throw new PrivacyException(PrivacyProperties.RESULTS_WITHHELD_FOR_PRIVACY_REASONS);
    }
    int count = ehrIds.size();

    String idsString = "'" + String.join("','", ehrIds) + "'";

    var hospitals = getSizesPerHospital(idsString);

    var ageGroups = getSizesPerAgeGroup(idsString);

    return CohortSizeDto.builder().hospitals(hospitals).ages(ageGroups).count(count).build();
  }

  private Map<String, Integer> getSizesPerAgeGroup(String idsString) {
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

  private Map<String, Integer> getSizesPerHospital(String idsString) {

    Map<String, Integer> sizes = new LinkedHashMap<>();
    List<String> clinics = contentService.getClinics();
    for (String clinic : clinics) {
      QueryResponseData queryResponseData =
          ehrBaseService.executePlainQuery(
              String.format(GET_PATIENTS_PER_CLINIC, clinic, idsString));
      List<List<Object>> rows = queryResponseData.getRows();
      if (rows == null) {
        sizes.put(clinic, 0);
      } else {
        sizes.put(clinic, rows.size());
      }
    }
    return sizes;
  }
}
