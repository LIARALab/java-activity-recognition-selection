/*
 * Copyright (C) 2019 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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
package org.liara.selection.datetime;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.ParsePosition;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.util.*;
import java.util.stream.Collectors;

public class PartialZonedDateTime
  implements TemporalAccessor,
             Comparable<PartialZonedDateTime>
{
  @NonNull
  public static final Set<@NonNull ChronoField> DATE_FIELDS = Collections.unmodifiableSet(
    Arrays.stream(ChronoField.values())
      .filter(ChronoField::isDateBased)
      .collect(Collectors.toSet())
  );

  @NonNull
  public static final Set<@NonNull ChronoField> TIME_FIELDS = Collections.unmodifiableSet(
    Arrays.stream(ChronoField.values())
      .filter(ChronoField::isTimeBased)
      .collect(Collectors.toSet())
  );

  private static final ChronoField[] FIELDS_BY_COMPARISON_PRIORITY = {
    ChronoField.ERA,
    ChronoField.YEAR_OF_ERA,
    ChronoField.YEAR,
    ChronoField.ALIGNED_WEEK_OF_YEAR,
    ChronoField.DAY_OF_YEAR,
    ChronoField.MONTH_OF_YEAR,
    ChronoField.PROLEPTIC_MONTH,
    ChronoField.DAY_OF_MONTH,
    ChronoField.DAY_OF_WEEK,
    ChronoField.MICRO_OF_DAY,
    ChronoField.MILLI_OF_DAY,
    ChronoField.SECOND_OF_DAY,
    ChronoField.MINUTE_OF_DAY,
    ChronoField.HOUR_OF_DAY,
    ChronoField.MINUTE_OF_HOUR,
    ChronoField.SECOND_OF_MINUTE,
    ChronoField.MICRO_OF_SECOND,
    ChronoField.MILLI_OF_SECOND,
    ChronoField.HOUR_OF_AMPM,
    ChronoField.ALIGNED_WEEK_OF_MONTH,
    ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH
  };

  @NonNull
  private final TemporalAccessor _rawDate;

  @NonNull
  private final TemporalAccessor _resolvedDate;

  private final boolean _isDate;

  private final boolean _isTime;

  private final boolean _isPartialDate;

  private final boolean _isPartialTime;

  protected PartialZonedDateTime (
    @NonNull final TemporalAccessor rawDate, @NonNull final TemporalAccessor resolvedDate
  ) {
    _rawDate = rawDate;
    _resolvedDate = resolvedDate;
    _isDate = checkIfIsDate();
    _isTime = checkIfIsTime();
    _isPartialDate = checkIfIsPartialDate();
    _isPartialTime = checkIfIsPartialTime();
  }

  public static @NonNull PartialZonedDateTime from (
    @NonNull final DateTimeFormatter format,
    @NonNull final String expression
  ) {
    return new PartialZonedDateTime(
      format.parseUnresolved(expression, new ParsePosition(0)),
      format.parse(expression)
    );
  }

  private static int getOrZero (
    @NonNull final TemporalAccessor accessor,
    @NonNull final TemporalField field
  ) {
    return accessor.isSupported(field) ? accessor.get(field) : 0;
  }

  private boolean checkIfIsDate () {
    return supportsAny(ChronoField.YEAR, ChronoField.YEAR_OF_ERA) && (
      supports(ChronoField.DAY_OF_YEAR) ||
      (
        supports(ChronoField.ALIGNED_WEEK_OF_YEAR) &&
        supportsAny(ChronoField.DAY_OF_WEEK, ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR)
      ) ||
      (
        supportsAny(ChronoField.MONTH_OF_YEAR, ChronoField.PROLEPTIC_MONTH) && (
          supports(ChronoField.DAY_OF_MONTH) ||
          supports(ChronoField.ALIGNED_WEEK_OF_MONTH, ChronoField.DAY_OF_WEEK) ||
          supports(ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH)
        )
      )
    );
  }

  private boolean supports (@NonNull final ChronoField... fields) {
    for (@NonNull final ChronoField field : fields) {
      if (!_rawDate.isSupported(field)) return false;
    }
    return true;
  }

  private boolean supportsAny (@NonNull final ChronoField... fields) {
    for (@NonNull final ChronoField field : fields) {
      if (_rawDate.isSupported(field)) return true;
    }
    return false;
  }

  private boolean checkIfIsPartialDate () {
    for (@NonNull final ChronoField field : PartialZonedDateTime.DATE_FIELDS) {
      if (_rawDate.isSupported(field)) return !_isDate;
    }
    return false;
  }

  private boolean checkIfIsPartialTime () {
    for (@NonNull final ChronoField field : PartialZonedDateTime.TIME_FIELDS) {
      if (_rawDate.isSupported(field)) return !_isTime;
    }
    return false;
  }

  public boolean checkIfIsTime () {
    return supportsAny(
      ChronoField.NANO_OF_DAY,
      ChronoField.MICRO_OF_DAY,
      ChronoField.MILLI_OF_DAY
    ) || (
             (
               supports(ChronoField.SECOND_OF_DAY) || (
                 (
                   supports(ChronoField.MINUTE_OF_DAY) || (
                     (
                       supportsAny(
                         ChronoField.HOUR_OF_DAY,
                         ChronoField.HOUR_OF_AMPM,
                         ChronoField.CLOCK_HOUR_OF_AMPM,
                         ChronoField.CLOCK_HOUR_OF_DAY
                       )
                     ) && supports(ChronoField.MINUTE_OF_HOUR)
                   )
                 ) && supports(ChronoField.SECOND_OF_MINUTE)
               )
             )
           );
  }

  public @NonNull ZoneId getZone () {
    return getZone(_rawDate);
  }

  public boolean isDate () { return _isDate; }

  public boolean isTime () { return _isTime; }

  public boolean isDateTime () { return _isDate && _isTime; }

  public boolean isPartialDate () { return _isPartialDate; }

  public boolean isPartialTime () { return _isPartialTime; }

  public boolean isPartial () { return _isPartialDate || _isPartialTime; }

  private @NonNull ZoneId getZone (@NonNull final TemporalAccessor accessor) {
    return Optional.ofNullable(accessor.query(TemporalQueries.zone()))
             .orElseGet(ZoneId::systemDefault);
  }

  private @NonNull ZonedDateTime toFilledZonedDateTime (@NonNull final PartialZonedDateTime base) {
    return ZonedDateTime.of(
      getOrZero(base.getResolvedAccessor(), ChronoField.YEAR),
      getOrZero(base.getResolvedAccessor(), ChronoField.MONTH_OF_YEAR),
      getOrZero(base.getResolvedAccessor(), ChronoField.DAY_OF_MONTH),
      getOrZero(base.getResolvedAccessor(), ChronoField.HOUR_OF_DAY),
      getOrZero(base.getResolvedAccessor(), ChronoField.MINUTE_OF_HOUR),
      getOrZero(base.getResolvedAccessor(), ChronoField.SECOND_OF_MINUTE),
      getOrZero(base.getResolvedAccessor(), ChronoField.NANO_OF_SECOND),
      base.getZone()
    );
  }

  private int mergeField (
    @NonNull final TemporalField field,
    @NonNull final PartialZonedDateTime base,
    @NonNull final ZonedDateTime filledBase,
    @NonNull final PartialZonedDateTime toMerge,
    @NonNull final ZonedDateTime filledToMerge
  ) {
    if (base.getResolvedAccessor().isSupported(field)) {
      return filledBase.get(field);
    } else if (toMerge.getResolvedAccessor().isSupported(field)) {
      return filledToMerge.get(field);
    } else {
      return 0;
    }
  }

  private @NonNull ZonedDateTime merge (
    @NonNull final PartialZonedDateTime base,
    @NonNull final ZonedDateTime filledBase,
    @NonNull final PartialZonedDateTime toMerge,
    @NonNull final ZonedDateTime filledToMerge
  ) {
    return ZonedDateTime.of(
      mergeField(ChronoField.YEAR, base, filledBase, toMerge, filledToMerge),
      mergeField(ChronoField.MONTH_OF_YEAR, base, filledBase, toMerge, filledToMerge),
      mergeField(ChronoField.DAY_OF_MONTH, base, filledBase, toMerge, filledToMerge),
      mergeField(ChronoField.HOUR_OF_DAY, base, filledBase, toMerge, filledToMerge),
      mergeField(ChronoField.MINUTE_OF_HOUR, base, filledBase, toMerge, filledToMerge),
      mergeField(ChronoField.SECOND_OF_MINUTE, base, filledBase, toMerge, filledToMerge),
      mergeField(ChronoField.NANO_OF_SECOND, base, filledBase, toMerge, filledToMerge),
      base.getZone()
    );
  }

  /**
   * @param other
   *
   * @return
   *
   * @Todo Smarter comparison based on zones.
   */
  @Override
  public int compareTo (@NonNull final PartialZonedDateTime other) {
    @NonNull final ZonedDateTime left  = toFilledZonedDateTime(this);
    @NonNull
    final ZonedDateTime          right = toFilledZonedDateTime(other).withZoneSameInstant(getZone());

    return merge(this, left, other, right).compareTo(merge(other, right, this, left));
  }

  public @NonNull Iterable<@NonNull ChronoField> fields () {
    return Collections.unmodifiableSet(
      Arrays.stream(ChronoField.values())
        .filter(field -> field.isSupportedBy(this))
        .collect(Collectors.toSet())
    );
  }

  public @NonNull Iterable<@NonNull ChronoField> dateFields () {
    return Collections.unmodifiableSet(
      PartialZonedDateTime.DATE_FIELDS.stream().filter(field -> field.isSupportedBy(this))
        .collect(Collectors.toSet())
    );
  }

  public @NonNull Iterable<@NonNull ChronoField> timeFields () {
    return Collections.unmodifiableSet(
      PartialZonedDateTime.TIME_FIELDS.stream().filter(field -> field.isSupportedBy(this))
        .collect(Collectors.toSet()));
  }

  @Override
  public long getLong (@NonNull final TemporalField field) {
    if (field.isDateBased() && _isDate) return _resolvedDate.getLong(field);
    else if (field.isTimeBased() && _isTime) return _resolvedDate.getLong(field);
    else return _rawDate.getLong(field);
  }

  @Override
  public boolean isSupported (@NonNull final TemporalField field) {
    if (field.isDateBased() && _isDate) return _resolvedDate.isSupported(field);
    else if (field.isTimeBased() && _isTime) return _resolvedDate.isSupported(field);
    else return _rawDate.isSupported(field);
  }

  public @NonNull TemporalAccessor getRawAccessor () {
    return _rawDate;
  }

  public @NonNull TemporalAccessor getResolvedAccessor () {
    return _resolvedDate;
  }

  @Override
  public int hashCode () {
    return _rawDate.hashCode();
  }

  @Override
  public boolean equals (@Nullable final Object object) {
    if (object == null) return false;
    if (object == this) return true;

    if (object instanceof PartialZonedDateTime) {
      @NonNull final PartialZonedDateTime other = (PartialZonedDateTime) object;

      return Objects.equals(_rawDate, other._rawDate);
    }

    return false;
  }
}
