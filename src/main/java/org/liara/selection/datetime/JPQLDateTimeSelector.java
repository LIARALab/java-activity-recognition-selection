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

package org.liara.selection.datetime;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

public final class JPQLDateTimeSelector
{
  @Nullable
  private static Map<ChronoField, Method> FACTORIES = null;

  private static void scanFactoriesIfNecessary () {
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

  public static boolean isSupported (@NonNull final ChronoField field) {
    JPQLDateTimeSelector.scanFactoriesIfNecessary();

    return JPQLDateTimeSelector.FACTORIES.containsKey(field);
  }

  public static @NonNull String select (@NonNull final ChronoField field, @NonNull final String expression) {
    JPQLDateTimeSelector.scanFactoriesIfNecessary();

    if (FACTORIES.containsKey(field)) {
      try {
        return (String) FACTORIES.get(field).invoke(null, expression);
      } catch (@NonNull final Exception exception) {
        throw new Error("Error during factory invokation.", exception);
      }
    } else {
      throw new InvalidParameterException("Unhandled field type : " + field);
    }
  }

  public static @NonNull String toDate (@NonNull final String expression) {
    return "DATE_FORMAT(" + expression + ", '%Y-%m-%d')";
  }

  public static @NonNull String toTime (@NonNull final String expression) {
    return "DATE_FORMAT(" + expression + ", '%H:%i:%s.%f')";
  }

  @Factory(ChronoField.MICRO_OF_SECOND)
  public static @NonNull String selectMicroOfSeconds (@NonNull final String expression) {
    return "MICROSECOND(" + expression + ")";
  }

  @Factory(ChronoField.MICRO_OF_DAY)
  public static @NonNull String selectMicroOfDay (@NonNull final String expression) {
    return "((HOUR(" + expression + ") * 60 + MINUTE(" + expression + ")) * 60 + SECOND(" + expression + ")) * " +
           "1000000) + MICROSECOND(" + expression + "))";
  }

  @Factory(ChronoField.MILLI_OF_SECOND)
  public static @NonNull String selectMilliOfSecond (@NonNull final String expression) {
    return "MICROSECOND(" + expression + ") / 1000";
  }

  @Factory(ChronoField.MILLI_OF_DAY)
  public static @NonNull String selectMilliOfDay (@NonNull final String expression) {
    return "((HOUR(" + expression + ") * 60 + MINUTE(" + expression + ")) * 60) + SECOND(" + expression + ")) * 1000)" +
           " + MICROSECOND(" + expression + ") / 1000)";
  }

  @Factory(ChronoField.SECOND_OF_MINUTE)
  public static @NonNull String selectSecondOfMinute (@NonNull final String expression) {
    return "SECOND(" + expression + ")";
  }

  @Factory(ChronoField.SECOND_OF_DAY)
  public static @NonNull String selectSecondOfDay (@NonNull final String expression) {
    return "((HOUR(" + expression + ") * 60 + MINUTE(" + expression + ")) * 60 + SECOND(" + expression + ")";
  }

  @Factory(ChronoField.MINUTE_OF_HOUR)
  public static @NonNull String selectMinuteOfHour (@NonNull final String expression) {
    return "MINUTE(" + expression + ")";
  }

  @Factory(ChronoField.MINUTE_OF_DAY)
  public static @NonNull String selectMinuteOfDay (@NonNull final String expression) {
    return "HOUR(" + expression + ") * 60 + MINUTE(" + expression + ")";
  }

  @Factory(ChronoField.HOUR_OF_DAY)
  public static @NonNull String selectHourOfDay (@NonNull final String expression) {
    return "HOUR(" + expression + ")";
  }

  @Factory(ChronoField.HOUR_OF_AMPM)
  public static @NonNull String selectHourOfAMPM (@NonNull final String expression) {
    return "HOUR(" + expression + ") % 12";
  }

  @Factory(ChronoField.DAY_OF_YEAR)
  public static @NonNull String selectDayOfYear (@NonNull final String expression) {
    return "DAYOFYEAR(" + expression + ")";
  }

  @Factory(ChronoField.DAY_OF_MONTH)
  public static @NonNull String selectDayOfMonth (@NonNull final String expression) {
    return "DAYOFMONTH(" + expression + ")";
  }

  @Factory(ChronoField.DAY_OF_WEEK)
  public static @NonNull String selectDayOfWeek (@NonNull final String expression) {
    return "DAYOFWEEK(" + expression + ")";
  }

  @Factory(ChronoField.ALIGNED_WEEK_OF_YEAR)
  public static @NonNull String selectWeekOfYear (@NonNull final String expression) {
    return "WEEK(" + expression + ")";
  }

  @Factory(ChronoField.MONTH_OF_YEAR)
  public static @NonNull String selectMonthOfYear (@NonNull final String expression) {
    return "MONTH(" + expression + ")";
  }

  @Factory(ChronoField.YEAR)
  public static @NonNull String selectYear (@NonNull final String expression) {
    return "YEAR(" + expression + ")";
  }

  @Retention(RetentionPolicy.RUNTIME)
  private @interface Factory
  {
    @NonNull ChronoField value ();
  }
}
