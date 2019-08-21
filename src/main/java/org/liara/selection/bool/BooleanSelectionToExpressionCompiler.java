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

package org.liara.selection.bool;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.data.primitive.Primitives;
import org.liara.expression.Expression;
import org.liara.expression.ExpressionFactory;
import org.liara.selection.SelectionToExpressionCompiler;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.TranspilationException;
import org.liara.selection.antlr.BooleanSelectionBaseListener;
import org.liara.selection.antlr.BooleanSelectionLexer;
import org.liara.selection.antlr.BooleanSelectionParser;

public class BooleanSelectionToExpressionCompiler
    extends BooleanSelectionBaseListener
    implements SelectionToExpressionCompiler {

  @NonNull
  private final ExpressionFactory _expressionFactory;

  @NonNull
  private final List<@NonNull Expression<@NonNull Boolean>> _stack;

  @NonNegative
  private int _offset;

  @NonNull
  private Expression<Boolean> _value;

  public BooleanSelectionToExpressionCompiler() {
    _stack = new ArrayList<>(20);
    _offset = 0;
    _expressionFactory = new ExpressionFactory();
    _value = _expressionFactory.placeholder(Primitives.BOOLEAN);
  }

  @Override
  public void enterSelection(
      final BooleanSelectionParser.@NonNull SelectionContext context
  ) {
    _stack.clear();
    _offset = 0;
  }

  @Override
  public void exitSelection(final BooleanSelectionParser.@NonNull SelectionContext context) {
    @NonNull final Expression<@NonNull Boolean> selection = _expressionFactory.or(_stack);
    _stack.clear();
    _stack.add(selection);
  }

  @Override
  public void enterFilter(final BooleanSelectionParser.@NonNull FilterContext context) {
    _offset = _stack.size();
  }

  @Override
  public void exitFilter(
      final BooleanSelectionParser.@NonNull FilterContext context
  ) {
    @NonNull final Expression<@NonNull Boolean> filter = _expressionFactory.and(
        _stack.subList(_offset, _stack.size())
    );
  }

  @Override
  public void exitNegation(
      final BooleanSelectionParser.@NonNull NegationContext context
  ) {
    _stack.set(_stack.size() - 1, _expressionFactory.not(_stack.get(_stack.size() - 1)));
  }

  @Override
  public void exitOperation(
      final BooleanSelectionParser.@NonNull OperationContext context
  ) {
    _stack.add(
        _expressionFactory.equal(
            _value,
            _expressionFactory.nullable(parse(context.target))
        )
    );
  }

  private @Nullable Boolean parse(@NonNull final Token target) {
    return (
        target.getType() == BooleanSelectionLexer.NULL
    ) ? null : target.getType() == BooleanSelectionLexer.TRUE;
  }

  public @NonNull Expression<@NonNull Boolean> transpile(@NonNull final CharSequence expression) {
    if (expression.toString().trim().equalsIgnoreCase("")) {
      return _expressionFactory.equal(
          _value,
          _expressionFactory.nonnull(true)
      );
    }

    @NonNull final BooleanSelectionLexer lexer = (
        new BooleanSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    @NonNull final BooleanSelectionParser parser = (
        new BooleanSelectionParser(new CommonTokenStream(lexer))
    );

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _stack.get(0);
  }

  public @NonNull Expression<@NonNull Boolean> tryToTranspile(
      @NonNull final CharSequence expression
  ) throws TranspilationException {
    if (expression.toString().trim().equalsIgnoreCase("")) {
      return _expressionFactory.equal(
          _value,
          _expressionFactory.nonnull(true)
      );
    }

    @NonNull final BooleanSelectionLexer lexer = (
        new BooleanSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    @NonNull final BooleanSelectionParser parser = (
        new BooleanSelectionParser(new CommonTokenStream(lexer))
    );

    lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
    parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _stack.get(0);
  }

  public @NonNull Expression<Boolean> getValue() {
    return _value;
  }

  public void setValue(@NonNull final Expression<Boolean> value) {
    _value = value;
  }
}
