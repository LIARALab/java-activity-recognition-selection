package org.liara.selection.jpql;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class JPQLQueryBuilder
{
  @NonNull
  private final StringBuilder _selection;

  @NonNull
  private final StringBuilder _filter;

  @NonNull
  private final Map<@NonNull String, @NonNull Object> _parameters;

  private int _clauses;

  public JPQLQueryBuilder () {
    _selection = new StringBuilder();
    _filter = new StringBuilder();
    _parameters = new HashMap<>();
    _clauses = 0;
  }

  public @NonNull Map<@NonNull String, @NonNull Object> getParameters () {
    return Collections.unmodifiableMap(_parameters);
  }

  public void appendClause (@NonNull final JPQLClauseBuilder builder) {
    appendClause(builder.getClause(), builder.getParameters());
  }

  public void appendClause (@NonNull final String clause) {
    if (_filter.length() > 0) {
      _filter.append(" AND ");
    }

    _filter.append(clause);
    _clauses++;
  }

  public void appendClause (@NonNull final String clause, @NonNull final Map<String, Object> parameters) {
    @NonNull final String namespace = "clause_" + _clauses;
    appendClause(clause.replaceAll(":(?!this)([a-zA-Z0-9_]+)", ":" + namespace + "_$1"));

    for (final Map.Entry<@NonNull String, @NonNull Object> parameter : parameters.entrySet()) {
      _parameters.put(namespace + "_" + parameter.getKey(), parameter.getValue());
    }
  }

  public void appendFilter () {
    _selection.append((_selection.length() > 0) ? " OR (" : "(");
    _selection.append(_filter.toString());
    _selection.append(")");

    _filter.setLength(0);
  }

  public void reset () {
    _selection.setLength(0);
    _filter.setLength(0);
    _parameters.clear();
    _clauses = 0;
  }

  public @NonNull JPQLQuery build () {
    @NonNull final String selection = _selection.toString();

    if (selection.contains("OR")) {
      return new JPQLQuery("(" + selection + ")", _parameters);
    } else {
      return new JPQLQuery(selection, _parameters);
    }
  }
}
