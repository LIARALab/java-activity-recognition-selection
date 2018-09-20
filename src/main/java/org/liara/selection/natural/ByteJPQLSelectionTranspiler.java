package org.liara.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ByteJPQLSelectionTranspiler
       extends NumberJPQLSelectionTranspiler<Byte>
{
  @Override
  protected @NonNull Byte parse (@NonNull final Token token) {
    try {
      return Byte.parseByte(token.getText().replaceFirst("\\.[0-9]+", ""));
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
  protected @NonNull Byte add (
    @NonNull final Byte left,
    @NonNull final Byte right
  ) { return (byte) (left + right); }

  @Override
  protected @NonNull Byte subtract (
    @NonNull final Byte left,
    @NonNull final Byte right
  ) { return (byte) (left - right); }
}
