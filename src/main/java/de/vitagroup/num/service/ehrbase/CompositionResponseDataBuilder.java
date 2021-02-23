package de.vitagroup.num.service.ehrbase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nedap.archie.rm.composition.Composition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.ehrbase.response.openehr.QueryResponseData;
import org.ehrbase.serialisation.jsonencoding.CanonicalJson;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CompositionResponseDataBuilder {
  private final ObjectMapper mapper;
  private final CompositionFlattener compositionFlattener;

  private static final String COLUMN_NAME = "name";
  private static final String COLUMN_PATH = "path";

  public QueryResponseData build(List<Map<String, String>> compositions) {
    List<Map<String, String>> flatCompositionsList = createCompositionsMap(compositions);
    Set<String> allKeys = new HashSet<>();
    flatCompositionsList.forEach(map -> allKeys.addAll(map.keySet()));

    Map<String, List<Object>> aggregatedResultsMap = new HashMap<>();
    List<List<Object>> rows = new ArrayList<>();
    List<Map<String, String>> columns = new ArrayList<>();

    allKeys.forEach(
        k -> {
          aggregatedResultsMap.put(k, new ArrayList<>());

          flatCompositionsList.forEach(
              map -> {
                if (map.containsKey(k)) {
                  aggregatedResultsMap.get(k).add(map.get(k));
                } else {
                  aggregatedResultsMap.get(k).add(null);
                }
              });
          createColumn(columns, k);
        });

    createRow(rows, aggregatedResultsMap, allKeys, flatCompositionsList.size());

    QueryResponseData queryResponseData = new QueryResponseData();
    queryResponseData.setColumns(columns);
    queryResponseData.setRows(rows);

    return queryResponseData;
  }

  private void createRow(List<List<Object>> rows, Map<String, List<Object>> aggregatedResultsMap, Set<String> allKeys, int rowsCount) {
    for (int i = 0; i < rowsCount; i++) {
      List<Object> values = new ArrayList<>();
      for (String k : allKeys) {
        List<Object> cols = aggregatedResultsMap.get(k);
        values.add(cols.get(i));
      }
      rows.add(values);
    }
  }

  private void createColumn(List<Map<String, String>> columns, String path) {
    Map<String, String> header = new HashMap<>();
    header.put(COLUMN_NAME, path);
    header.put(COLUMN_PATH, path);
    columns.add(header);
  }

  public List<Map<String, String>> createCompositionsMap(List<Map<String, String>> compositions) {
    List<Map<String, String>> compositionsMap = new ArrayList<>();

    compositions.forEach(
        compositionMap -> {
          String compositionString;
          try {
            compositionString = mapper.writeValueAsString(compositionMap);
            Composition composition =
                new CanonicalJson().unmarshal(compositionString, Composition.class);
            compositionsMap.add(compositionFlattener.flatten(composition));
          } catch (JsonProcessingException e) {
            e.printStackTrace();
          }
        });

    return compositionsMap;
  }
}
