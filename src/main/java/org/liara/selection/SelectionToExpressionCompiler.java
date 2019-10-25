/*
 * Copyright (C) 2019 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
 *
 * Permission is hereby granted,  free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,  including without limitation the rights
 * to use,  copy, modify, merge,  publish,  distribute, sublicense,  and/or sell
 * copies  of the  Software, and  to  permit persons  to  whom  the  Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above  copyright  notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED,  INCLUDING  BUT  NOT LIMITED  TO THE  WARRANTIES  OF MERCHANTABILITY,
 * FITNESS  FOR  A PARTICULAR  PURPOSE  AND  NONINFRINGEMENT. IN NO  EVENT SHALL
 * THE  AUTHORS OR  COPYRIGHT  HOLDERS  BE  LIABLE FOR  ANY  CLAIM,  DAMAGES  OR
 * OTHER  LIABILITY, WHETHER  IN  AN  ACTION  OF  CONTRACT,  TORT  OR  OTHERWISE,
 * ARISING  FROM,  OUT  OF OR  IN  CONNECTION  WITH THE  SOFTWARE OR  THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.liara.selection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.expression.Expression;

public interface SelectionToExpressionCompiler<Result> {

  /**
   * Compile the given boolean selection into an expression.
   *
   * @param selection A boolean selection.
   * @return An expression built from the given selection.
   */
  @NonNull Expression<@NonNull Boolean> compile(@NonNull final CharSequence selection);

  /**
   * Try to compile the given boolean selection into an expression and throws an error on any
   * lexical or grammatical exception.
   *
   * @param selection A boolean selection.
   * @return An expression built from the given selection.
   * @throws CompilationException If any lexical or grammatical exception is spot by the lexer or
   * the parser.
   */
  @NonNull Expression<@NonNull Boolean> tryToCompile(
      @NonNull final CharSequence selection
  ) throws CompilationException;
}
