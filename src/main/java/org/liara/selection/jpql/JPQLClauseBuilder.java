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
