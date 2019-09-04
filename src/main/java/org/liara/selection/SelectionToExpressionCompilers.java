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

package org.liara.selection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.test.selection.bool.BooleanSelectionToExpressionCompiler;
import org.liara.test.selection.datetime.DateTimeInRangeSelectionToExpressionCompiler;
import org.liara.test.selection.datetime.DateTimeSelectionToExpressionCompiler;
import org.liara.test.selection.duration.DurationSelectionToExpressionCompiler;
import org.liara.test.selection.natural.ByteSelectionToExpressionCompiler;
import org.liara.test.selection.natural.DoubleSelectionToExpressionCompiler;
import org.liara.test.selection.natural.FloatSelectionToExpressionCompiler;
import org.liara.test.selection.natural.IntegerSelectionToExpressionCompiler;
import org.liara.test.selection.natural.LongSelectionToExpressionCompiler;
import org.liara.test.selection.natural.ShortSelectionToExpressionCompiler;
import org.liara.test.selection.string.StringSelectionToExpressionCompiler;

public final class SelectionToExpressionCompilers {

  public static @NonNull IntegerSelectionToExpressionCompiler integerTranspiler() {
    return new IntegerSelectionToExpressionCompiler();
  }

  public static @NonNull LongSelectionToExpressionCompiler longTranspiler() {
    return new LongSelectionToExpressionCompiler();
  }

  public static @NonNull ShortSelectionToExpressionCompiler shortTranspiler() {
    return new ShortSelectionToExpressionCompiler();
  }

  public static @NonNull ByteSelectionToExpressionCompiler byteTranspiler() {
    return new ByteSelectionToExpressionCompiler();
  }

  public static @NonNull FloatSelectionToExpressionCompiler floatTranspiler() {
    return new FloatSelectionToExpressionCompiler();
  }

  public static @NonNull DoubleSelectionToExpressionCompiler doubleTranspiler() {
    return new DoubleSelectionToExpressionCompiler();
  }

  public static @NonNull StringSelectionToExpressionCompiler stringTranspiler() {
    return new StringSelectionToExpressionCompiler();
  }

  public static @NonNull DurationSelectionToExpressionCompiler durationTranspiler() {
    return new DurationSelectionToExpressionCompiler();
  }

  public static @NonNull DateTimeSelectionToExpressionCompiler datetimeTranspiler() {
    return new DateTimeSelectionToExpressionCompiler();
  }

  public static @NonNull BooleanSelectionToExpressionCompiler getBoolean() {
    return new BooleanSelectionToExpressionCompiler();
  }

  public static @NonNull DateTimeInRangeSelectionToExpressionCompiler datetimeInRangeTranspiler() {
    return new DateTimeInRangeSelectionToExpressionCompiler();
  }
}
