package org.liara.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;

public class DoubleJPQLSelectionTranspiler
       extends NumberJPQLSelectionTranspiler<Double>
{
  @Override
  protected @NonNull Double parse (@NonNull final Token token) {
    try {
      return Double.parseDouble(token.getText());
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
  protected @NonNull Double add (
    @NonNull final Double left,
    @NonNull final Double right
  ) { return left + right; }

  @Override
  protected @NonNull Double subtract (
    @NonNull final Double left,
    @NonNull final Double right
  ) { return left - right; }
}
