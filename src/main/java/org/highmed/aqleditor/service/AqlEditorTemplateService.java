package org.highmed.aqleditor.service;

import lombok.AllArgsConstructor;
import org.highmed.aqleditor.dto.template.TemplateDto;
import org.ehrbase.openehr.sdk.client.openehrclient.defaultrestclient.DefaultRestClient;
import org.ehrbase.openehr.sdk.response.dto.TemplatesResponseData;
import org.ehrbase.openehr.sdk.webtemplate.filter.Filter;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.ehrbase.openehr.sdk.webtemplate.parser.OPTParser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AqlEditorTemplateService {

  private final DefaultRestClient restClient;

  public List<TemplateDto> getAll() {
    TemplatesResponseData responseData = restClient.templateEndpoint().findAllTemplates();
    return responseData.get().stream()
        .map(templateMetaDataDto -> TemplateDto.builder()
            .templateId(templateMetaDataDto.getTemplateId())
            .description(templateMetaDataDto.getConcept()).build())
        .collect(Collectors.toList());
  }

  public WebTemplate getWebTemplate(String templateId) {
    return restClient.templateEndpoint().findTemplate(templateId)
        .map(o -> new OPTParser(o).parse())
        .map(w -> new Filter().filter(w))
        .orElse(null);
  }

}
