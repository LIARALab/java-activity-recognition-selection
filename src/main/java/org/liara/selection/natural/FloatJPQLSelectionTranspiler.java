package org.liara.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;

public class FloatJPQLSelectionTranspiler
       extends NumberJPQLSelectionTranspiler<Float>
{
  @Override
  protected @NonNull Float parse (@NonNull final Token token) {
    try {
      return Float.parseFloat(token.getText());
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
  protected @NonNull Float add (
    @NonNull final Float left,
    @NonNull final Float right
  ) { return left + right; }

  @Override
  protected @NonNull Float subtract (
    @NonNull final Float left,
    @NonNull final Float right
  ) { return left - right; }
}
