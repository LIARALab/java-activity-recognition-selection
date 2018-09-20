/*******************************************************************************
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.liara.selection.datetime;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.selection.jpql.JPQLClauseBuilder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;

public class PartialZonedDateTime
  implements TemporalAccessor, Comparable<TemporalAccessor>
{  
  @NonNull
  public static final ChronoField[] DATETIME_FIELDS = new ChronoField[] {
    ChronoField.YEAR,
    ChronoField.MONTH_OF_YEAR,
    ChronoField.DAY_OF_MONTH,
    ChronoField.HOUR_OF_DAY,
    ChronoField.MINUTE_OF_HOUR,
    ChronoField.SECOND_OF_MINUTE,
    ChronoField.MICRO_OF_SECOND
  };

  @NonNull
  public static final ChronoField[] CONTEXT_FIELDS = new ChronoField[] {
    ChronoField.ALIGNED_WEEK_OF_YEAR,
    ChronoField.ALIGNED_WEEK_OF_MONTH,
    ChronoField.DAY_OF_YEAR,
    ChronoField.DAY_OF_WEEK
  };

  @NonNull
  public static final ChronoField[] COMPARISON_FIELDS = new ChronoField[] {
    ChronoField.YEAR,
    ChronoField.MONTH_OF_YEAR,
    ChronoField.ALIGNED_WEEK_OF_YEAR,
    ChronoField.ALIGNED_WEEK_OF_MONTH,
    ChronoField.DAY_OF_YEAR,
    ChronoField.DAY_OF_MONTH,
    ChronoField.DAY_OF_WEEK,
    ChronoField.HOUR_OF_DAY,
    ChronoField.MINUTE_OF_HOUR,
    ChronoField.SECOND_OF_MINUTE,
    ChronoField.MICRO_OF_SECOND
  };
  
  @NonNull
  private final TemporalAccessor _date;
  
  public static @NonNull PartialZonedDateTime from (@NonNull final TemporalAccessor date) {
    return new PartialZonedDateTime(date);
  }
  
  protected PartialZonedDateTime (@NonNull final TemporalAccessor date) {
    _date = date;
  }

  public void appendMaskedDateTo (@NonNull final JPQLClauseBuilder clause) {
    clause.append("STR_TO_DATE(DATE_FORMAT(");
    clause.append(clause.self());
    clause.append(",");
    clause.appendParameter("mask", getSQLMask());
    clause.append("), '%Y-%m-%d %H:%i:%s.%f')");
  }


  public void appendField (
    @NonNull final JPQLClauseBuilder clause,
    @NonNull final ChronoField field
  ) {
    switch (field) {
      case MICRO_OF_SECOND:
        clause.appendLiteral("MICROSECOND(" + clause.self() + ")");
      case SECOND_OF_MINUTE:
        clause.appendLiteral("SECOND(" + clause.self() + ")");
      case MINUTE_OF_HOUR:
        clause.appendLiteral("MINUTE(" + clause.self() + ")");
      case HOUR_OF_DAY:
        clause.appendLiteral("HOUR(" + clause.self() + ")");
      case DAY_OF_YEAR:
        clause.appendLiteral("DAYOFYEAR(" + clause.self() + ")");
      case DAY_OF_MONTH:
        clause.appendLiteral("DAYOFMONTH(" + clause.self() + ")");
      case DAY_OF_WEEK:
        clause.appendLiteral("DAYOFWEEK(" + clause.self() + ")");
      case ALIGNED_WEEK_OF_YEAR:
        clause.appendLiteral("WEEKOFYEAR(" + clause.self() + ")");
      case ALIGNED_WEEK_OF_MONTH:
        clause.appendLiteral("WEEK(" + clause.self() + ")");
      case MONTH_OF_YEAR:
        clause.appendLiteral("MONTH(" + clause.self() + ")");
      case YEAR:
        clause.appendLiteral("YEAR(" + clause.self() + ")");
      default:
        throw new Error("Unhandled field " + field);
    }
  }

  @Override
  public int compareTo (@NonNull final TemporalAccessor other) {
    for (final ChronoField field : COMPARISON_FIELDS) {
      if (isSupported(field) && other.isSupported(field)) {
        final long myValue = this.getLong(field);
        final long otherValue = other.getLong(field);
        
        if (myValue < otherValue) {
          return -1;
        } else if (myValue > otherValue) {
          return 1;
        }
      }
    }
    
    return 0;
  }
  
  public @NonNull ZonedDateTime toZonedDateTime () {
    @NonNull ZonedDateTime result = ZonedDateTime.of(
      1900, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()
    );
    
    for (@NonNull final ChronoField field : new ChronoField[] {
      ChronoField.YEAR,
      ChronoField.MONTH_OF_YEAR,
      ChronoField.DAY_OF_MONTH,
      ChronoField.HOUR_OF_DAY,
      ChronoField.MINUTE_OF_HOUR,
      ChronoField.SECOND_OF_MINUTE,
      ChronoField.MICRO_OF_SECOND
    }) {
      if (isSupported(field)) {
        result = result.with(field, getLong(field));
      }
    }
    
    return result;
  }
  
  public boolean isCompleteZonedDateTime () {
    for (@NonNull final ChronoField field : COMPARISON_FIELDS) {
      if (!isSupported(field)) {
        return false;
      }
    }
    
    return true;
  }

  @Override
  public long getLong (@NonNull final TemporalField field) {
    return this._date.getLong(field);
  }

  @Override
  public boolean isSupported (@NonNull final TemporalField field) {
    return this._date.isSupported(field);
  }
  
  public @NonNull String getSQLMask () {
    final StringBuilder builder = new StringBuilder();
    
    builder.append((isSupported(ChronoField.YEAR)) ? "%Y" : "1900");
    builder.append("-");
    builder.append((isSupported(ChronoField.MONTH_OF_YEAR)) ? "%m" : "01");
    builder.append("-");
    builder.append((isSupported(ChronoField.DAY_OF_MONTH)) ? "%d" : "01");
    builder.append(" ");
    builder.append((isSupported(ChronoField.HOUR_OF_DAY)) ? "%H" : "00");
    builder.append(":");
    builder.append((isSupported(ChronoField.MINUTE_OF_HOUR)) ? "%i" : "00");
    builder.append(":");
    builder.append((isSupported(ChronoField.SECOND_OF_MINUTE)) ? "%s" : "00");
    builder.append(".");
    builder.append((isSupported(ChronoField.MICRO_OF_SECOND)) ? "%f" : "000000");
    
    return builder.toString();
  }
  
  public boolean containsDatetime () {
    for (@NonNull final ChronoField field : PartialZonedDateTime.DATETIME_FIELDS) {
      if (isSupported(field)) return true;
    }
    
    return false;
  }
  
  public boolean containsContext () {
    for (@NonNull final ChronoField field : PartialZonedDateTime.CONTEXT_FIELDS) {
      if (isSupported(field)) return true;
    }
    
    return false;
  }

  @Override
  public @NonNull String toString () {
    @NonNull final StringBuilder builder = new StringBuilder();
    
    builder.append((isSupported(ChronoField.YEAR)) ? getLong(ChronoField.YEAR) : "****");
    builder.append("-");
    builder.append((isSupported(ChronoField.MONTH_OF_YEAR)) ? getLong(ChronoField.MONTH_OF_YEAR) : "**");
    builder.append("-");
    builder.append((isSupported(ChronoField.DAY_OF_MONTH)) ? getLong(ChronoField.DAY_OF_MONTH) : "**");
    builder.append("T");
    builder.append((isSupported(ChronoField.HOUR_OF_DAY)) ? getLong(ChronoField.HOUR_OF_DAY) : "**");
    builder.append(":");
    builder.append((isSupported(ChronoField.MINUTE_OF_HOUR)) ? getLong(ChronoField.MINUTE_OF_HOUR) : "**");
    builder.append(":");
    builder.append((isSupported(ChronoField.SECOND_OF_MINUTE)) ? getLong(ChronoField.SECOND_OF_MINUTE) : "**");
    builder.append(".");
    builder.append((isSupported(ChronoField.MICRO_OF_SECOND)) ? getLong(ChronoField.MICRO_OF_SECOND) : "******");
    
    return builder.toString();
  }
}
