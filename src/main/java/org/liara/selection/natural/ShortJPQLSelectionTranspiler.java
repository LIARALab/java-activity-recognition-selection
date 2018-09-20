package org.liara.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShortJPQLSelectionTranspiler
       extends NumberJPQLSelectionTranspiler<Short>
{
  @Override
  protected @NonNull Short parse (@NonNull final Token token) {
    try {
      return Short.parseShort(token.getText().replaceFirst("\\.[0-9]+", ""));
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
  protected @NonNull Short add (
    @NonNull final Short left,
    @NonNull final Short right
  ) { return (short) (left + right); }

  @Override
  protected @NonNull Short subtract (
    @NonNull final Short left,
    @NonNull final Short right
  ) { return (short) (left - right); }
}
