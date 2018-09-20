package org.liara.selection.jpql;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JPQLQuery
{
  @NonNull
  private final String _clause;

  @NonNull
  private final Map<@NonNull String, @NonNull Object> _parameters;

  public static @NonNull JPQLQuery query (
    @NonNull final String query,
    @NonNull final Map<@NonNull String, @NonNull Object> parameters
  ) {
    return new JPQLQuery(query, parameters);
  }

  public JPQLQuery (
    @NonNull final String clause,
    @NonNull final Map<@NonNull String, @NonNull Object> parameter
  ) {
    _clause = clause;
    _parameters = new HashMap<>(parameter);
  }

  public JPQLQuery (@NonNull final JPQLQuery toCopy) {
    _clause = toCopy.getClause();
    _parameters = new HashMap<>(toCopy.getParameters());
  }

  public @NonNull String getClause () {
    return _clause;
  }

  public @NonNull JPQLQuery setClause (@NonNull final String clause) {
    return new JPQLQuery(clause, _parameters);
  }

  public @NonNull Map<@NonNull String, @NonNull Object> getParameters () {
    return Collections.unmodifiableMap(_parameters);
  }

  public @NonNull JPQLQuery setParameters (@NonNull final Map<@NonNull String, @NonNull Object> parameters) {
    return new JPQLQuery(_clause, parameters);
  }

  @Override
  public int hashCode () {
    return Objects.hash(_clause, _parameters);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof JPQLQuery) {
      @NonNull final JPQLQuery otherResult = (JPQLQuery) other;

      return Objects.equals(_clause, otherResult.getClause()) &&
             Objects.equals(_parameters, otherResult.getParameters());
    }

    return false;
  }
}
