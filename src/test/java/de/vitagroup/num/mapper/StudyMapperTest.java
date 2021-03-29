/*
 * Copyright 2021. Vitagroup AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vitagroup.num.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import de.vitagroup.num.domain.Cohort;
import de.vitagroup.num.domain.Study;
import de.vitagroup.num.domain.StudyStatus;
import de.vitagroup.num.domain.admin.User;
import de.vitagroup.num.domain.admin.UserDetails;
import de.vitagroup.num.domain.dto.StudyDto;
import de.vitagroup.num.domain.dto.TemplateInfoDto;
import de.vitagroup.num.service.UserDetailsService;
import de.vitagroup.num.service.UserService;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

@RunWith(MockitoJUnitRunner.class)
public class StudyMapperTest {

  @Spy private ModelMapper modelMapper;

  @Mock private TemplateMapper templateMapper;

  @InjectMocks private StudyMapper studyMapper;

  @Mock
  private UserService userService;

  @Before
  public void setup() {
    studyMapper.initialize();
    when(templateMapper.convertToTemplateInfoDtoList(any()))
        .thenReturn(
            List.of(
                TemplateInfoDto.builder().templateId("param1").name("value1").build(),
                TemplateInfoDto.builder().templateId("param2").name("value2").build()));

    when(userService.getUserById("123", false)).thenReturn(User.builder().build());
  }

  @Test
  public void shouldCorrectlyConvertStudyToStudyDto() {
    Cohort cohort = Cohort.builder().id(1L).build();

    Study study =
        Study.builder()
            .id(1L)
            .name("Study name")
            .cohort(cohort)
            .description("Study description")
            .firstHypotheses("first")
            .secondHypotheses("second")
            .status(StudyStatus.DRAFT)
            .coordinator(UserDetails.builder().userId("123").build())
            .templates(Map.of("param1", "value1", "param2", "value2"))
            .build();

    StudyDto dto = studyMapper.convertToDto(study);

    assertThat(dto, notNullValue());

    assertThat(dto.getName(), is(study.getName()));
    assertThat(dto.getDescription(), is(study.getDescription()));
    assertThat(dto.getFirstHypotheses(), is(study.getFirstHypotheses()));
    assertThat(dto.getSecondHypotheses(), is(study.getSecondHypotheses()));
    assertThat(dto.getCohortId(), is(cohort.getId()));
    assertThat(dto.getTemplates().stream().anyMatch(c -> c.getTemplateId() == "param1"), is(true));
    assertThat(dto.getTemplates().stream().anyMatch(c -> c.getTemplateId() == "param2"), is(true));
    assertThat(dto.getId(), is(study.getId()));
  }
}
