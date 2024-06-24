package org.highmed.aqleditor.service;

import com.nedap.archie.rm.archetyped.Locatable;
import com.nedap.archie.rm.composition.EventContext;
import com.nedap.archie.rm.datavalues.quantity.DvInterval;
import com.nedap.archie.rminfo.ArchieRMInfoLookup;
import com.nedap.archie.rminfo.RMAttributeInfo;
import com.nedap.archie.rminfo.RMTypeInfo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.highmed.aqleditor.dto.containment.ContainmentDto;
import org.highmed.aqleditor.dto.containment.FieldDto;
import org.ehrbase.openehr.sdk.aql.webtemplatepath.AqlPath;
import org.ehrbase.openehr.sdk.generator.config.RmClassGeneratorConfig;
import org.ehrbase.openehr.sdk.util.SnakeCase;
import org.ehrbase.openehr.sdk.util.reflection.ReflectionHelper;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplate;
import org.ehrbase.openehr.sdk.webtemplate.model.WebTemplateNode;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

@Service
@AllArgsConstructor
public class AqlEditorContainmentService {

  public static final ArchieRMInfoLookup ARCHIE_RM_INFO_LOOKUP = ArchieRMInfoLookup.getInstance();
  private static final Map<Class<?>, RmClassGeneratorConfig> configMap =
      ReflectionHelper.buildMap(RmClassGeneratorConfig.class);

  private AqlEditorTemplateService aqlEditorTemplateService;

  public ContainmentDto buildContainment(String templateId) {

    WebTemplate webTemplate = aqlEditorTemplateService.getWebTemplate(templateId);
    Context context = new Context();

    handleNext(context, webTemplate.getTree());

    return context.containmentQueue.getFirst();
  }

  private void handleNext(Context context, WebTemplateNode childNode) {
    if (visitChildren(childNode)) {
      if (context.containmentQueue.isEmpty()
          || (childNode.getNodeId() != null && !childNode.getNodeId().startsWith("at"))) {
        ContainmentDto containmentDto = new ContainmentDto();
        containmentDto.setArchetypeId(childNode.getNodeId());
        if (!context.containmentQueue.isEmpty()) {
          context.containmentQueue.peek().getChildren().add(containmentDto);
        }
        context.containmentQueue.push(containmentDto);
        context.aqlQueue.push(childNode.getAqlPath());
        context.nodeQueue.push(childNode);
        childNode.getChildren().forEach(n -> handleNext(context, n));
        context.nodeQueue.remove();
        context.aqlQueue.remove();
        if (context.containmentQueue.size() > 1) {
          context.containmentQueue.remove();
        }
      } else {
        context.nodeQueue.push(childNode);
        childNode.getChildren().forEach(n -> handleNext(context, n));
        context.nodeQueue.remove();
      }

    } else {
      RMTypeInfo typeInfo = ARCHIE_RM_INFO_LOOKUP.getTypeInfo(childNode.getRmType());
      RmClassGeneratorConfig rmClassGeneratorConfig = configMap.get(typeInfo.getJavaClass());
      if (rmClassGeneratorConfig == null || !rmClassGeneratorConfig.isExpandField()) {
        FieldDto fieldDto = new FieldDto();
        fieldDto.setName(childNode.getName());
        fieldDto.setRmType(childNode.getRmType());
        String relativAql =
            StringUtils.removeStart(childNode.getAqlPath(), context.aqlQueue.peek());
        fieldDto.setAqlPath(AqlPath.parse(relativAql).format(false));
        context.nodeQueue.push(childNode);
        fieldDto.setHumanReadablePath(buildHumanReadablePath(context));
        context.containmentQueue.peek().getFields().add(fieldDto);
        context.nodeQueue.remove();
      } else {
        for (String fieldName : rmClassGeneratorConfig.getExpandFields()) {
          RMAttributeInfo rmAttributeInfo =
              typeInfo.getAttributes().get(new SnakeCase(fieldName).camelToSnake());
          FieldDto fieldDto = new FieldDto();
          fieldDto.setName(childNode.getName() + "::" + rmAttributeInfo.getRmName());
          fieldDto.setRmType(rmAttributeInfo.getTypeNameInCollection());
          String relativAql =
              StringUtils.removeStart(childNode.getAqlPath(), context.aqlQueue.peek());
          fieldDto.setAqlPath(
              AqlPath.parse(relativAql + "/" + rmAttributeInfo.getRmName()).format(false));
          context.nodeQueue.push(childNode);
          fieldDto.setHumanReadablePath(
              buildHumanReadablePath(context) + "/" + rmAttributeInfo.getRmName());
          context.containmentQueue.peek().getFields().add(fieldDto);
          context.nodeQueue.remove();
        }
      }
    }
  }

  private String buildHumanReadablePath(Context context) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<WebTemplateNode> iterator = context.nodeQueue.descendingIterator();
        iterator.hasNext(); ) {
      WebTemplateNode node = iterator.next();
      sb.append(node.getId());
      if (iterator.hasNext()) {
        sb.append("/");
      }
    }
    return sb.toString();
  }

  protected boolean visitChildren(WebTemplateNode node) {
    RMTypeInfo typeInfo = ARCHIE_RM_INFO_LOOKUP.getTypeInfo(node.getRmType());
    return typeInfo != null
        && (Locatable.class.isAssignableFrom(typeInfo.getJavaClass())
        || EventContext.class.isAssignableFrom(typeInfo.getJavaClass())
        || DvInterval.class.isAssignableFrom(typeInfo.getJavaClass()));
  }

  private static class Context {

    Deque<WebTemplateNode> nodeQueue = new ArrayDeque<>();
    Deque<ContainmentDto> containmentQueue = new ArrayDeque<>();
    Deque<String> aqlQueue = new ArrayDeque<>();
  }
}
