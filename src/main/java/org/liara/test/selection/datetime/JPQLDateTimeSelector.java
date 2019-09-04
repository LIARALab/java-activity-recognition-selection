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

package org.liara.test.selection.datetime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.data.primitive.Primitives;
import org.liara.expression.Expression;
import org.liara.expression.ExpressionFactory;

public final class JPQLDateTimeSelector {

  @Nullable
  private static Map<ChronoField, Method> FACTORIES = null;

  @NonNull
  private static ExpressionFactory EXPRESSION_FACTORY = new ExpressionFactory();

  private static void scanFactoriesIfNecessary() {
    if (JPQLDateTimeSelector.FACTORIES == null) {
      JPQLDateTimeSelector.FACTORIES = new HashMap<>();

      for (@NonNull final Method method : JPQLDateTimeSelector.class.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Factory.class)) {
          @NonNull final Factory factoryAnnotation = method.getAnnotation(Factory.class);
          JPQLDateTimeSelector.FACTORIES.put(factoryAnnotation.value(), method);
        }
      }
    }
  }

  public static boolean isSupported(@NonNull final ChronoField field) {
    JPQLDateTimeSelector.scanFactoriesIfNecessary();

    return JPQLDateTimeSelector.FACTORIES.containsKey(field);
  }

  public static @NonNull Expression<@NonNull Long> select(
      @NonNull final TemporalField field,
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    if (field instanceof ChronoField) {
      return select((ChronoField) field, expression);
    } else if (field == IsoFields.WEEK_BASED_YEAR) {
      return selectWeekBasedYear(expression);
    } else {
      throw new InvalidParameterException("Unhandled field type : " + field);
    }
  }

  public static @NonNull Expression<@NonNull Long> select(
      @NonNull final ChronoField field,
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    JPQLDateTimeSelector.scanFactoriesIfNecessary();

    if (FACTORIES.containsKey(field)) {
      try {
        return (Expression<@NonNull Long>) FACTORIES.get(field).invoke(null, expression);
      } catch (@NonNull final Exception exception) {
        throw new Error("Error during factory execution.", exception);
      }
    } else {
      throw new InvalidParameterException("Unhandled field type : " + field);
    }
  }

  public static @NonNull Expression<@NonNull ZonedDateTime> zone(
      @NonNull final Expression<@NonNull ZonedDateTime> expression,
      @NonNull final ZoneId zone
  ) {
    @NonNull final String from = TimeZone.getDefault().getID();
    @NonNull final String to = zone.getId();

    if (from.equalsIgnoreCase(to)) {
      return expression;
    } else {
      return EXPRESSION_FACTORY.function(
          Primitives.DATE_TIME,
          "CONVERT_TZ",
          Arrays.asList(
              expression,
              EXPRESSION_FACTORY.nonnull(from),
              EXPRESSION_FACTORY.nonnull(to)
          )
      );
    }
  }

  public static @NonNull Expression<@NonNull LocalDate> toDate(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.DATE,
        "DATE_FORMAT",
        Arrays.asList(
            expression,
            EXPRESSION_FACTORY.nonnull("%Y-%m-%d")
        )
    );
  }

  public static @NonNull Expression<@NonNull LocalTime> toTime(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.TIME,
        "DATE_FORMAT",
        Arrays.asList(
            expression,
            EXPRESSION_FACTORY.nonnull("%H:%i:%s.%f")
        )
    );
  }

  @Factory(ChronoField.MICRO_OF_SECOND)
  public static @NonNull Expression<@NonNull Long> selectMicroOfSeconds(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "MICROSECOND",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.MICRO_OF_DAY)
  public static @NonNull Expression<@NonNull Long> selectMicroOfDay(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.add(
        EXPRESSION_FACTORY.multiply(
            selectSecondOfDay(expression),
            EXPRESSION_FACTORY.nonnull(1_000_000L)
        ),
        selectMicroOfSeconds(expression)
    );
  }

  @Factory(ChronoField.MILLI_OF_SECOND)
  public static @NonNull Expression<@NonNull Long> selectMilliOfSecond(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.divide(
        selectMicroOfSeconds(expression),
        EXPRESSION_FACTORY.nonnull(1_000L)
    );
  }

  @Factory(ChronoField.MILLI_OF_DAY)
  public static @NonNull Expression<@NonNull Long> selectMilliOfDay(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.add(
        EXPRESSION_FACTORY.multiply(
            selectSecondOfDay(expression),
            EXPRESSION_FACTORY.nonnull(1_000L)
        ),
        selectMilliOfSecond(expression)
    );
  }

  @Factory(ChronoField.SECOND_OF_MINUTE)
  public static @NonNull Expression<@NonNull Long> selectSecondOfMinute(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "SECOND",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.SECOND_OF_DAY)
  public static @NonNull Expression<@NonNull Long> selectSecondOfDay(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.add(
        EXPRESSION_FACTORY.multiply(
            selectMinuteOfDay(expression),
            EXPRESSION_FACTORY.nonnull(60L)
        ),
        selectSecondOfMinute(expression)
    );
  }

  @Factory(ChronoField.MINUTE_OF_HOUR)
  public static @NonNull Expression<@NonNull Long> selectMinuteOfHour(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "MINUTE",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.MINUTE_OF_DAY)
  public static @NonNull Expression<@NonNull Long> selectMinuteOfDay(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.add(
        EXPRESSION_FACTORY.multiply(
            selectHourOfDay(expression),
            EXPRESSION_FACTORY.nonnull(60L)
        ),
        selectMinuteOfHour(expression)
    );
  }

  @Factory(ChronoField.HOUR_OF_DAY)
  public static @NonNull Expression<@NonNull Long> selectHourOfDay(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "HOUR",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.HOUR_OF_AMPM)
  public static @NonNull Expression<@NonNull Long> selectHourOfAMPM(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.modulus(
        selectHourOfDay(expression),
        EXPRESSION_FACTORY.nonnull(12L)
    );
  }

  @Factory(ChronoField.DAY_OF_YEAR)
  public static @NonNull Expression<@NonNull Long> selectDayOfYear(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "DAYOFYEAR",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.DAY_OF_MONTH)
  public static @NonNull Expression<@NonNull Long> selectDayOfMonth(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "DAYOFMONTH",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.DAY_OF_WEEK)
  public static @NonNull Expression<@NonNull Long> selectDayOfWeek(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "DAYOFWEEK",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.ALIGNED_WEEK_OF_YEAR)
  public static @NonNull Expression<@NonNull Long> selectWeekOfYear(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "WEEK",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.MONTH_OF_YEAR)
  public static @NonNull Expression<@NonNull Long> selectMonthOfYear(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "MONTH",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.YEAR)
  public static @NonNull Expression<@NonNull Long> selectYear(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "YEAR",
        Collections.singletonList(expression)
    );
  }

  @Factory(ChronoField.YEAR_OF_ERA)
  public static @NonNull Expression<@NonNull Long> selectYearOfEra(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "YEAR",
        Collections.singletonList(expression)
    );
  }

  public static @NonNull Expression<@NonNull Long> selectWeekBasedYear(
      @NonNull final Expression<@NonNull ZonedDateTime> expression
  ) {
    return EXPRESSION_FACTORY.function(
        Primitives.LONG,
        "YEAR",
        Collections.singletonList(expression)
    );
  }

  @Retention(RetentionPolicy.RUNTIME)
  private @interface Factory {

    @NonNull ChronoField value();
  }
}
