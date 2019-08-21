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
import org.liara.selection.bool.BooleanSelectionToExpressionCompiler;
import org.liara.selection.datetime.DateTimeInRangeJPQLSelectionTranspiler;
import org.liara.selection.datetime.DateTimeJPQLSelectionTranspiler;
import org.liara.selection.duration.DurationSelectionToExpressionCompiler;
import org.liara.selection.natural.ByteSelectionToExpressionCompiler;
import org.liara.selection.natural.DoubleSelectionToExpressionCompiler;
import org.liara.selection.natural.FloatSelectionToExpressionCompiler;
import org.liara.selection.natural.IntegerSelectionToExpressionCompiler;
import org.liara.selection.natural.LongSelectionToExpressionCompiler;
import org.liara.selection.natural.ShortSelectionToExpressionCompiler;
import org.liara.selection.string.StringSelectionToExpressionCompiler;

public final class SelectionToExpressionCompilers {

  public static @NonNull SelectionToExpressionCompiler integerTranspiler() {
    return new IntegerSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler longTranspiler() {
    return new LongSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler shortTranspiler() {
    return new ShortSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler byteTranspiler() {
    return new ByteSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler floatTranspiler() {
    return new FloatSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler doubleTranspiler() {
    return new DoubleSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler stringTranspiler() {
    return new StringSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler durationTranspiler() {
    return new DurationSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler datetimeTranspiler() {
    return new DateTimeJPQLSelectionTranspiler();
  }

  public static @NonNull SelectionToExpressionCompiler getBoolean() {
    return new BooleanSelectionToExpressionCompiler();
  }

  public static @NonNull SelectionToExpressionCompiler datetimeInRangeTranspiler() {
    return new DateTimeInRangeJPQLSelectionTranspiler();
  }
}
