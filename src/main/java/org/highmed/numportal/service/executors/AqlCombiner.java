package org.highmed.numportal.service.executors;

import org.highmed.numportal.domain.model.CohortGroup;
import org.highmed.numportal.domain.model.Operator;
import org.highmed.numportal.domain.model.Type;
import org.highmed.numportal.service.util.AqlQueryConstants;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.dto.condition.ComparisonOperatorCondition;
import org.ehrbase.openehr.sdk.aql.dto.condition.ExistsCondition;
import org.ehrbase.openehr.sdk.aql.dto.condition.LikeCondition;
import org.ehrbase.openehr.sdk.aql.dto.condition.LogicalOperatorCondition;
import org.ehrbase.openehr.sdk.aql.dto.condition.MatchesCondition;
import org.ehrbase.openehr.sdk.aql.dto.condition.NotCondition;
import org.ehrbase.openehr.sdk.aql.dto.condition.WhereCondition;
import org.ehrbase.openehr.sdk.aql.dto.containment.AbstractContainmentExpression;
import org.ehrbase.openehr.sdk.aql.dto.containment.Containment;
import org.ehrbase.openehr.sdk.aql.dto.containment.ContainmentClassExpression;
import org.ehrbase.openehr.sdk.aql.dto.containment.ContainmentNotOperator;
import org.ehrbase.openehr.sdk.aql.dto.containment.ContainmentSetOperator;
import org.ehrbase.openehr.sdk.aql.dto.containment.ContainmentSetOperatorSymbol;
import org.ehrbase.openehr.sdk.aql.dto.operand.ComparisonLeftOperand;
import org.ehrbase.openehr.sdk.aql.dto.operand.IdentifiedPath;
import org.ehrbase.openehr.sdk.aql.dto.operand.LikeOperand;
import org.ehrbase.openehr.sdk.aql.dto.operand.Operand;
import org.ehrbase.openehr.sdk.aql.dto.operand.QueryParameter;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectClause;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectExpression;
import org.ehrbase.openehr.sdk.aql.parser.AqlQueryParser;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class AqlCombiner {

  public AqlWithParams combineQuery(CohortGroup cohortGroup) {
    P p = new P();
    AqlQuery aqlQuery = combineQuery(cohortGroup, p);

    // add fake SELECT so render() works, will be replaced later
    aqlQuery.setSelect(getSelectClause(AqlQueryConstants.COMPOSITION_TYPE, "e/ehr_id/value", null));

    // add "FROM EHR e"
    Containment from = aqlQuery.getFrom();
    ContainmentClassExpression containmentClassExpression1 = new ContainmentClassExpression();
    containmentClassExpression1.setType("EHR");
    containmentClassExpression1.setIdentifier("e");
    containmentClassExpression1.setContains(from);
    aqlQuery.setFrom(containmentClassExpression1);

    return new AqlWithParams(aqlQuery, p.parameters);
  }

  private AqlQuery combineQuery(CohortGroup cohortGroup, P p) {
    if (cohortGroup.getType() == Type.GROUP) {
      var c = cohortGroup.getChildren().stream()
              .map(cohortGroup1 -> combineQuery(cohortGroup1, p))
              .toList();

      AqlQuery aqlQuery = new AqlQuery();

      if (cohortGroup.getOperator() == Operator.AND || cohortGroup.getOperator() == Operator.OR) {
        ContainmentSetOperator from = new ContainmentSetOperator();
        from.setSymbol(cohortGroup.getOperator() == Operator.AND ? ContainmentSetOperatorSymbol.AND :
                ContainmentSetOperatorSymbol.OR);
        from.setValues(c.stream().map(AqlQuery::getFrom).collect(Collectors.toList()));
        aqlQuery.setFrom(from);

        LogicalOperatorCondition where = new LogicalOperatorCondition();
        where.setSymbol(cohortGroup.getOperator() == Operator.AND ? LogicalOperatorCondition.ConditionLogicalOperatorSymbol.AND :
                LogicalOperatorCondition.ConditionLogicalOperatorSymbol.OR);
        where.setValues(c.stream().map(AqlQuery::getWhere).collect(Collectors.toList()));
        aqlQuery.setWhere(where);

      } else if (cohortGroup.getOperator() == Operator.NOT) {
        aqlQuery.setFrom(c.get(0).getFrom());

        NotCondition not = new NotCondition();
        not.setConditionDto(c.get(0).getWhere());
        aqlQuery.setWhere(not);
      } else {
        throw new IllegalStateException("Unexpected value: " + cohortGroup.getOperator());
      }
      return aqlQuery;

    } else if (cohortGroup.getType() == Type.AQL) {
      AqlQuery parse = AqlQueryParser.parse(cohortGroup.getQuery().getQuery());

      // assert that "FROM EHR e" exists and ignore it
      var item = parse.getFrom();
      assert item instanceof ContainmentClassExpression;
      assert Objects.equals(((ContainmentClassExpression) item).getType(), "EHR");
      assert Objects.equals(((ContainmentClassExpression) item).getIdentifier(), "e");
      assert Objects.equals(((ContainmentClassExpression) item).getPredicates(), null);
      parse.setFrom(((ContainmentClassExpression) item).getContains());

      // queries may contain the same parameter names and field names, so use a unique prefix in every input query
      String prefix = "aql" + ++p.i + "_";
      p.parameters.putAll(cohortGroup.getParameters().entrySet().stream()
              .map(entry -> new AbstractMap.SimpleEntry<>(prefix + entry.getKey(), entry.getValue()))
              // can't use Collectors.toMap because it doesn't support null values
              .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll)
      );
      renameParameter(parse.getWhere(), prefix);

      // - different queries may contain the same identifiers, so use a unique prefix in every input query
      // - the same ContainmentExpression can be referenced multiple times, so we can't change it in the rename methods
      //   because then the prefix might be prepended more than once, instead we store it in a set and change it here
      Set<AbstractContainmentExpression> containmentExpressions = new HashSet<>();
      renameField(parse.getWhere(), containmentExpressions);
      renameField(parse.getFrom(), containmentExpressions);
      containmentExpressions.forEach(abstractContainmentExpression ->
              abstractContainmentExpression.setIdentifier(prefix + abstractContainmentExpression.getIdentifier()));

      return parse;
    }
    return new AqlQuery();
  }

  private void renameParameter(WhereCondition condition, String prefix) {
    if (condition instanceof LogicalOperatorCondition logicalOperatorCondition) {
      List<WhereCondition> values = logicalOperatorCondition.getValues();
      values.forEach(whereCondition -> renameParameter(whereCondition, prefix));
    } else if (condition instanceof NotCondition notCondition) {
      renameParameter(notCondition.getConditionDto(), prefix);
    } else if (condition instanceof ComparisonOperatorCondition comparisonOperatorCondition) {
      Operand value = comparisonOperatorCondition.getValue();
      if (value instanceof QueryParameter queryParameter) {
        queryParameter.setName(prefix + queryParameter.getName());
      }
    } else if (condition instanceof MatchesCondition matchesCondition) {
      matchesCondition.getValues().forEach(matchesOperand -> {
        if (matchesOperand instanceof QueryParameter queryParameter) {
          queryParameter.setName(prefix + queryParameter.getName());
        }
      });
    } else if (condition instanceof LikeCondition likeCondition) {
      LikeOperand value = likeCondition.getValue();
      if (value instanceof QueryParameter queryParameter) {
        queryParameter.setName(prefix + queryParameter.getName());
      }
    } else if (condition instanceof ExistsCondition) {
      // doesn't have parameter
    } else {
      if (condition != null) {
        throw new IllegalStateException("Unexpected condition: " + condition);
      }
    }
  }

  private void renameField(Containment containment, Set<AbstractContainmentExpression> expressions) {
    if (containment instanceof AbstractContainmentExpression abstractContainmentExpression) {
      expressions.add(abstractContainmentExpression);
      renameField(abstractContainmentExpression.getContains(), expressions);
    } else if (containment instanceof ContainmentSetOperator containmentSetOperator) {
      containmentSetOperator.getValues().forEach(containment1 -> renameField(containment1, expressions));
    } else if (containment instanceof ContainmentNotOperator containmentNotOperator) {
      renameField(containmentNotOperator.getContainmentExpression(), expressions);
    } else {
      if (containment != null) {
        throw new IllegalStateException("Unexpected containment: " + containment);
      }
    }
  }

  private void renameField(WhereCondition condition, Set<AbstractContainmentExpression> expressions) {
    if (condition instanceof LogicalOperatorCondition logicalOperatorCondition) {
      List<WhereCondition> values = logicalOperatorCondition.getValues();
      values.forEach(whereCondition -> renameField(whereCondition, expressions));
    } else if (condition instanceof NotCondition notCondition) {
      renameField(notCondition.getConditionDto(), expressions);
    } else if (condition instanceof ComparisonOperatorCondition comparisonOperatorCondition) {
      ComparisonLeftOperand value = comparisonOperatorCondition.getStatement();
      if (value instanceof IdentifiedPath identifiedPath) {
        expressions.add(identifiedPath.getRoot());
      }
    } else if (condition instanceof MatchesCondition matchesCondition) {
      IdentifiedPath value = matchesCondition.getStatement();
      expressions.add(value.getRoot());
    } else if (condition instanceof LikeCondition likeCondition) {
      IdentifiedPath value = likeCondition.getStatement();
      expressions.add(value.getRoot());
    } else if (condition instanceof ExistsCondition existsCondition) {
      IdentifiedPath value = existsCondition.getValue();
      expressions.add(value.getRoot());
    } else {
      if (condition != null) {
        throw new IllegalStateException("Unexpected condition: " + condition);
      }
    }
  }

  private SelectClause getSelectClause(String type, String identifier, String alias) {
    ContainmentClassExpression containmentClassExpression = new ContainmentClassExpression();
    containmentClassExpression.setType(type);
    containmentClassExpression.setIdentifier(identifier);
    IdentifiedPath identifiedPath = new IdentifiedPath();
    identifiedPath.setRoot(containmentClassExpression);

    // generate select expression
    SelectClause selectClause = new SelectClause();
    SelectExpression se = new SelectExpression();
    se.setColumnExpression(identifiedPath);
    se.setAlias(alias);
    selectClause.setStatement(List.of(se));
    return selectClause;
  }

  static class P {
    Map<String, Object> parameters = new HashMap<>();
    int i = 0;
  }
}
