package org.liara.selection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.selection.duration.DurationJPQLSelectionTranspiler;
import org.liara.selection.jpql.JPQLSelectionTranspiler;
import org.liara.selection.natural.*;
import org.liara.selection.string.StringJPQLSelectionTranspiler;

public final class JPQLSelection
{
  public static @NonNull JPQLSelectionTranspiler integerTranspiler () {
    return new IntegerJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler longTranspiler () {
    return new LongJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler bigIntegerTranspiler () {
    return new BigIntegerJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler shortTranspiler () {
    return new ShortJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler byteTranspiler () {
    return new ByteJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler floatTranspiler () {
    return new FloatJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler doubleTranspiler () {
    return new DoubleJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler bigDecimalTranspiler () {
    return new BigDecimalJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler stringTranspiler () {
    return new StringJPQLSelectionTranspiler();
  }

  public static @NonNull JPQLSelectionTranspiler durationTranspiler () {
    return new DurationJPQLSelectionTranspiler();
  }
}
