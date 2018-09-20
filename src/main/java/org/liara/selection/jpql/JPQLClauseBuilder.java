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

import java.util.HashMap;
import java.util.Map;

public class JPQLClauseBuilder
{
  @NonNull
  private final StringBuilder _builder;

  private boolean _negated;

  @NonNull
  private final Map<String, Object> _parameters;

  public JPQLClauseBuilder () {
    _negated = false;
    _builder = new StringBuilder();
    _parameters = new HashMap<>();
  }

  public void negate () {
    _negated = !_negated;
  }

  public void appendParameter (@NonNull final String name) {
    if (_builder.length() > 0) _builder.append(' ');

    _builder.append(':');
    _builder.append(name);
  }

  public void appendParameter (@NonNull final String name, @NonNull final Object value) {
    appendParameter(name);
    _parameters.put(name, value);
  }

  public void appendSelf () {
    appendLiteral(self());
  }

  public @NonNull String self () {
    return ":this";
  }

  public void appendLiteral (@NonNull final String literal) {
    if (_builder.length() > 0) _builder.append(' ');
    _builder.append(literal);
  }

  public void append (@NonNull final String token) {
    _builder.append(token);
  }

  public void setNegated (final boolean negated) {
    _negated = negated;
  }

  public boolean isNegated () {
    return _negated;
  }

  public @NonNull String getClause () {
    return (_negated) ? "NOT (" + _builder.toString() + ")" : _builder.toString();
  }

  public @NonNull Map<@NonNull String, @NonNull Object> getParameters () {
    return new HashMap<>(_parameters);
  }

  public void reset () {
    _parameters.clear();
    _builder.setLength(0);
    _negated = false;
  }
}
