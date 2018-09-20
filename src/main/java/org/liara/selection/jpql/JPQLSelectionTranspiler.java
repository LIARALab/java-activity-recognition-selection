package org.liara.selection.jpql;

import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface JPQLSelectionTranspiler
  extends ParseTreeListener
{
  @NonNull JPQLQuery transpile (@NonNull final CharSequence expression);
}
