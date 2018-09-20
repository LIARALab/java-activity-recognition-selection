package org.liara.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.math.BigDecimal;

public class BigDecimalJPQLSelectionTranspiler
       extends NumberJPQLSelectionTranspiler<BigDecimal>
{
  @Override
  protected @NonNull BigDecimal parse (@NonNull final Token token) {
    try {
      return new BigDecimal(token.getText());
    } catch (@NonNull final NumberFormatException exception) {
      throw new Error(
        String.join(
          "",
          "Invalid number format at line ", String.valueOf(token.getLine()), " and index ",
          String.valueOf(token.getCharPositionInLine()), " : \"", token.getText(), "\""
        ),
        exception
      );
    }
  }

  @Override
  protected @NonNull BigDecimal add (
    @NonNull final BigDecimal left,
    @NonNull final BigDecimal right
  ) { return left.add(right); }

  @Override
  protected @NonNull BigDecimal subtract (
    @NonNull final BigDecimal left,
    @NonNull final BigDecimal right
  ) { return left.subtract(right); }
}
