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

package org.liara.test.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.data.primitive.Primitives;

public class DoubleSelectionToExpressionCompiler
    extends NumberSelectionToExpressionCompiler<Double> {

  public DoubleSelectionToExpressionCompiler() {
    super(Primitives.DOUBLE);
  }

  @Override
  protected @NonNull Double parse(@NonNull final Token token) {
    try {
      return Double.parseDouble(token.getText());
    } catch (@NonNull final NumberFormatException exception) {
      throw new Error(
          String.join("",
              "Invalid number format at line ", String.valueOf(token.getLine()), " and index ",
              String.valueOf(token.getCharPositionInLine()), " : \"", token.getText(), "\""
          ),
          exception
      );
    }
  }

  @Override
  protected @NonNull Double add(
      @NonNull final Double left,
      @NonNull final Double right
  ) {
    return left + right;
  }

  @Override
  protected @NonNull Double subtract(
      @NonNull final Double left,
      @NonNull final Double right
  ) {
    return left - right;
  }
}