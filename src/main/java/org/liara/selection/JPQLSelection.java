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

package org.liara.selection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.selection.datetime.DateTimeInRangeJPQLSelectionTranspiler;
import org.liara.selection.datetime.DateTimeJPQLSelectionTranspiler;
import org.liara.selection.duration.DurationJPQLSelectionTranspiler;
import org.liara.selection.jpql.JPQLSelectionTranspiler;
import org.liara.selection.natural.*;
import org.liara.selection.string.StringJPQLSelectionTranspiler;

public final class JPQLSelection
{
  public static @NonNull JPQLSelectionTranspiler integerTranspiler () {
    return new IntegerJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler longTranspiler () {
    return new LongJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler bigIntegerTranspiler () {
    return new BigIntegerJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler shortTranspiler () {
    return new ShortJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler byteTranspiler () {
    return new ByteJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler floatTranspiler () {
    return new FloatJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler doubleTranspiler () {
    return new DoubleJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler bigDecimalTranspiler () {
    return new BigDecimalJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler stringTranspiler () {
    return new StringJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler durationTranspiler () {
    return new DurationJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler datetimeTranspiler () {
    return new DateTimeJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler datetimeInRangeTranspiler () {
    return new DateTimeInRangeJPQLSelectionTranspiler();
  }
}
