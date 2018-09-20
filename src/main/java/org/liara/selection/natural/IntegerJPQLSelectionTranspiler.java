package org.liara.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;

public class   IntegerJPQLSelectionTranspiler
       extends NumberJPQLSelectionTranspiler<Integer>
{
  @Override
  protected @NonNull Integer add (
    @NonNull final Integer left,
    @NonNull final Integer right
  ) { return left + right; }

  @Override
  protected @NonNull Integer subtract (
    @NonNull final Integer left,
    @NonNull final Integer right
  ) { return left - right; }

  @Override
  protected @NonNull Integer parse (@NonNull final Token token) {
    try {
      return Integer.parseInt(token.getText().replaceFirst("\\.[0-9]+", ""));
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
}
