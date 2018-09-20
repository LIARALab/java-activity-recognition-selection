package org.liara.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LongJPQLSelectionTranspiler
       extends NumberJPQLSelectionTranspiler<Long>
{
  @Override
  protected @NonNull Long parse (@NonNull final Token token) {
    try {
      return Long.parseLong(token.getText().replaceFirst("\\.[0-9]+", ""));
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
  protected @NonNull Long add (
    @NonNull final Long left,
    @NonNull final Long right
  ) { return left + right; }

  @Override
  protected @NonNull Long subtract (
    @NonNull final Long left,
    @NonNull final Long right
  ) { return left - right; }
}
