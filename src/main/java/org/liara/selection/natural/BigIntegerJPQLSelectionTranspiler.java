package org.liara.selection.natural;

import org.antlr.v4.runtime.Token;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.math.BigInteger;

public class   BigIntegerJPQLSelectionTranspiler
       extends NumberJPQLSelectionTranspiler<BigInteger>
{
  @Override
  protected @NonNull BigInteger add (
    @NonNull final BigInteger left,
    @NonNull final BigInteger right
  ) { return left.add(right); }

  @Override
  protected @NonNull BigInteger subtract (
    @NonNull final BigInteger left,
    @NonNull final BigInteger right
  ) { return left.subtract(right); }

  @Override
  protected @NonNull BigInteger parse (@NonNull final Token token) {
    try {
      return new BigInteger(token.getText().replaceFirst("\\.[0-9]+", ""));
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
