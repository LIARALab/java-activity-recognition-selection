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

import com.google.common.collect.Iterators;
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

public class PartialDate
  implements TemporalAccessor, Comparable<PartialDate>
{
  public static final @NonNull ChronoField[] DATE_PARTIALS_BY_COMPARISON_PRIORITY = {
    ChronoField.EPOCH_DAY,
    ChronoField.INSTANT_SECONDS,
    ChronoField.ERA,
    ChronoField.YEAR_OF_ERA,
    ChronoField.YEAR,
    ChronoField.DAY_OF_YEAR,
    ChronoField.ALIGNED_WEEK_OF_YEAR,
    ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR,
    ChronoField.MONTH_OF_YEAR,
    ChronoField.PROLEPTIC_MONTH,
    ChronoField.ALIGNED_WEEK_OF_MONTH,
    ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH,
    ChronoField.DAY_OF_MONTH,
    ChronoField.DAY_OF_WEEK
  };

  public static final @NonNull ChronoField[] TIME_PARTIALS_BY_COMPARISON_PRIORITY = {
    ChronoField.MICRO_OF_DAY,
    ChronoField.MILLI_OF_DAY,
    ChronoField.SECOND_OF_DAY,
    ChronoField.MINUTE_OF_DAY,
    ChronoField.HOUR_OF_DAY,
    ChronoField.HOUR_OF_AMPM,
    ChronoField.MINUTE_OF_HOUR,
    ChronoField.SECOND_OF_MINUTE,
    ChronoField.MICRO_OF_SECOND,
    ChronoField.MILLI_OF_SECOND
  };

  @NonNull
  private final TemporalAccessor _rawDate;

  @NonNull
  private final TemporalAccessor _resolvedDate;

  protected PartialDate (
    @NonNull final TemporalAccessor rawDate,
    @NonNull final TemporalAccessor resolvedDate
  ) {
    _rawDate = rawDate;
    _resolvedDate = resolvedDate;
  }

  public static @NonNull PartialDate from (
    @NonNull final DateTimeFormatter format,
    @NonNull final String expression
  ) {
    return new PartialDate(
      format.parseUnresolved(expression, new ParsePosition(0)),
      format.parse(expression)
    );
  }

  public boolean supportsDateTime () { return supportsDate() && supportsTime(); }

  public boolean supportsDate () {
    return supportsAny(ChronoField.EPOCH_DAY, ChronoField.INSTANT_SECONDS) ||
           supportsYearBasedDate();
  }

  public boolean supportsYearBasedDate () {
    return supportsAny(ChronoField.YEAR, ChronoField.YEAR_OF_ERA) && supportsDayOfYear();
  }

  public boolean supportsDayOfYear () {
    return supports(ChronoField.DAY_OF_YEAR) ||
           supportsWeekBasedDayOfYear() ||
           supportsMonthBasedDayOfYear();
  }

  public boolean supportsMonthBasedDayOfYear () {
    return supportsAny(ChronoField.MONTH_OF_YEAR, ChronoField.PROLEPTIC_MONTH) && (
      supports(ChronoField.DAY_OF_MONTH) ||
      supportsWeekBasedDayOfMonth()
    );
  }

  public boolean supportsWeekBasedDayOfMonth () {
    return supports(ChronoField.ALIGNED_WEEK_OF_MONTH) && supportsAny(
      ChronoField.DAY_OF_WEEK,
      ChronoField.ALIGNED_DAY_OF_WEEK_IN_MONTH
    );
  }

  public boolean supportsWeekBasedDayOfYear () {
    return supports(ChronoField.ALIGNED_WEEK_OF_YEAR) && supportsAny(
      ChronoField.DAY_OF_WEEK,
      ChronoField.ALIGNED_DAY_OF_WEEK_IN_YEAR
    );
  }

  public boolean supports (@NonNull final ChronoField... fields) {
    for (@NonNull final ChronoField field : fields) {
      if (!_rawDate.isSupported(field)) return false;
    }
    return true;
  }

  public boolean supportsAny (@NonNull final ChronoField... fields) {
    for (@NonNull final ChronoField field : fields) {
      if (_rawDate.isSupported(field)) return true;
    }
    return false;
  }

  public boolean supportsTime () {
    return supportsAny(
      ChronoField.NANO_OF_DAY,
      ChronoField.MICRO_OF_DAY,
      ChronoField.MILLI_OF_DAY
    ) || supportsSecondOfDay();
  }

  public boolean supportsSecondOfDay () {
    return supports(ChronoField.SECOND_OF_DAY) || (
      supportsMinuteOfDay() && supports(ChronoField.SECOND_OF_MINUTE)
    );
  }

  public boolean supportsMinuteOfDay () {
    return supports(ChronoField.MINUTE_OF_DAY) || (
      supportsHourOfDay() && supports(ChronoField.MINUTE_OF_HOUR)
    );
  }

  public boolean supportsHourOfDay () {
    return supportsAny(
      ChronoField.HOUR_OF_DAY,
      ChronoField.HOUR_OF_AMPM,
      ChronoField.CLOCK_HOUR_OF_AMPM,
      ChronoField.CLOCK_HOUR_OF_DAY
    );
  }

  public @NonNull ZoneId getZone () {
    return Optional.ofNullable(_rawDate.query(TemporalQueries.zone()))
             .orElseGet(ZoneId::systemDefault);
  }

  public boolean supportsPartialDate () {
    if (supportsDate()) return false;

    for (@NonNull final ChronoField field : PartialDate.DATE_PARTIALS_BY_COMPARISON_PRIORITY) {
      if (_rawDate.isSupported(field)) return true;
    }

    return false;
  }

  public boolean supportsPartialTime () {
    if (supportsTime()) return false;

    for (@NonNull final ChronoField field : PartialDate.TIME_PARTIALS_BY_COMPARISON_PRIORITY) {
      if (_rawDate.isSupported(field)) return true;
    }

    return false;
  }

  public boolean supportsPartials () {
    return supportsPartialTime() || supportsPartialDate();
  }

  private int getOrZero (
    @NonNull final TemporalAccessor accessor,
    @NonNull final TemporalField field
  ) {
    return accessor.isSupported(field) ? accessor.get(field)
                                       : Math.max((int) field.range().getMinimum(), 0);
  }

  private @NonNull ZonedDateTime toFilledZonedDateTime (@NonNull final PartialDate base) {
    return ZonedDateTime.of(
      getOrZero(base, ChronoField.YEAR),
      getOrZero(base, ChronoField.MONTH_OF_YEAR),
      getOrZero(base, ChronoField.DAY_OF_MONTH),
      getOrZero(base, ChronoField.HOUR_OF_DAY),
      getOrZero(base, ChronoField.MINUTE_OF_HOUR),
      getOrZero(base, ChronoField.SECOND_OF_MINUTE),
      getOrZero(base, ChronoField.NANO_OF_SECOND),
      base.getZone()
    );
  }

  private int mergeField (
    @NonNull final TemporalField field,
    @NonNull final PartialDate base,
    @NonNull final ZonedDateTime filledBase,
    @NonNull final PartialDate toMerge,
    @NonNull final ZonedDateTime filledToMerge
  ) {
    if (base.isSupported(field)) {
      return filledBase.get(field);
    } else if (toMerge.isSupported(field)) {
      return filledToMerge.get(field);
    } else {
      return Math.max((int) field.range().getMinimum(), 0);
    }
  }

  private @NonNull ZonedDateTime merge (
    @NonNull final PartialDate base,
    @NonNull final ZonedDateTime filledBase,
    @NonNull final PartialDate toMerge,
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

  @Override
  public int compareTo (@NonNull final PartialDate other) {
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

  public @NonNull Iterator<@NonNull ChronoField> partialDateFields () {
    return Arrays.stream(PartialDate.DATE_PARTIALS_BY_COMPARISON_PRIORITY)
             .filter(field -> field.isSupportedBy(this))
             .iterator();
  }

  public @NonNull Iterator<@NonNull ChronoField> partialTimeFields () {
    return Arrays.stream(PartialDate.TIME_PARTIALS_BY_COMPARISON_PRIORITY)
             .filter(field -> field.isSupportedBy(this))
             .iterator();
  }

  public @NonNull Iterator<@NonNull ChronoField> partialFields () {
    @NonNull final List<@NonNull Iterator<@NonNull ChronoField>> partialFields = new ArrayList<>(2);

    if (supportsPartialDate()) partialFields.add(partialDateFields());
    if (supportsPartialTime()) partialFields.add(partialTimeFields());

    return Iterators.concat(partialFields.iterator());
  }

  @Override
  public long getLong (@NonNull final TemporalField field) {
    if (field.isDateBased() && supportsDate()) return _resolvedDate.getLong(field);
    else if (field.isTimeBased() && supportsTime()) return _resolvedDate.getLong(field);
    else return _rawDate.getLong(field);
  }

  @Override
  public boolean isSupported (@NonNull final TemporalField field) {
    if (field.isDateBased() && supportsDate()) return _resolvedDate.isSupported(field);
    else if (field.isTimeBased() && supportsTime()) return _resolvedDate.isSupported(field);
    else return _rawDate.isSupported(field);
  }

  @Override
  public int hashCode () {
    return _rawDate.hashCode();
  }

  @Override
  public boolean equals (@Nullable final Object object) {
    if (object == null) return false;
    if (object == this) return true;

    if (object instanceof PartialDate) {
      @NonNull final PartialDate other = (PartialDate) object;

      return Objects.equals(_rawDate, other._rawDate);
    }

    return false;
  }
}
