package org.liara.selection;

import org.checkerframework.checker.nullness.qual.NonNull;

public final class Utils
{
  public static <Target extends Comparable<? super Target>> Target max (
    @NonNull final Target a,
    @NonNull final Target b
  ) {
    return (a.compareTo(b) > 0) ? a : b;
  }

  public static <Target extends Comparable<? super Target>> Target min (
    @NonNull final Target a,
    @NonNull final Target b
  ) {
    return (a.compareTo(b) < 0) ? a : b;
  }
}