/*
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
 *
 * Permission is hereby granted,  free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,  including without limitation the rights
 * to use,  copy, modify, merge,  publish,  distribute, sublicense,  and/or sell
 * copies  of the  Software, and  to  permit persons  to  whom  the  Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above  copyright  notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED,  INCLUDING  BUT  NOT LIMITED  TO THE  WARRANTIES  OF MERCHANTABILITY,
 * FITNESS  FOR  A PARTICULAR  PURPOSE  AND  NONINFRINGEMENT. IN NO  EVENT SHALL
 * THE  AUTHORS OR  COPYRIGHT  HOLDERS  BE  LIABLE FOR  ANY  CLAIM,  DAMAGES  OR
 * OTHER  LIABILITY, WHETHER  IN  AN  ACTION  OF  CONTRACT,  TORT  OR  OTHERWISE,
 * ARISING  FROM,  OUT  OF OR  IN  CONNECTION  WITH THE  SOFTWARE OR  THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.liara.selection.jpql;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
