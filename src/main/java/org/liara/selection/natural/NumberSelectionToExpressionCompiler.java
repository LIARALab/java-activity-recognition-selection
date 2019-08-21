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

package org.liara.selection.natural;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.data.primitive.Primitive;
import org.liara.expression.Expression;
import org.liara.expression.ExpressionFactory;
import org.liara.selection.SelectionToExpressionCompiler;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.TranspilationException;
import org.liara.selection.Utils;
import org.liara.selection.antlr.NumberSelectionBaseListener;
import org.liara.selection.antlr.NumberSelectionLexer;
import org.liara.selection.antlr.NumberSelectionParser;

public abstract class NumberSelectionToExpressionCompiler<Value extends Comparable<Value>>
    extends NumberSelectionBaseListener
    implements SelectionToExpressionCompiler {

  @NonNull
  private final ExpressionFactory _expressionFactory;

  @NonNull
  private final List<@NonNull Expression<@NonNull Boolean>> _stack;
  @NonNull
  private final Primitive<Value> _type;
  @NonNegative
  private int _offset;
  @NonNull
  private Expression<Value> _value;

  public NumberSelectionToExpressionCompiler(@NonNull final Primitive<Value> type) {
    _expressionFactory = new ExpressionFactory();
    _stack = new ArrayList<>(20);
    _offset = 0;
    _value = _expressionFactory.placeholder(type);
    _type = type;
  }

  @Override
  public void enterSelection(final NumberSelectionParser.@NonNull SelectionContext context) {
    _stack.clear();
    _offset = 0;
  }

  @Override
  public void exitSelection(final NumberSelectionParser.@NonNull SelectionContext context) {
    @NonNull final Expression<@NonNull Boolean> selection = _expressionFactory.or(_stack);
    _stack.clear();
    _stack.add(selection);
  }

  @Override
  public void enterFilter(final NumberSelectionParser.@NonNull FilterContext context) {
    _offset = _stack.size();
  }

  @Override
  public void exitFilter(final NumberSelectionParser.@NonNull FilterContext context) {
    @NonNull final Expression<@NonNull Boolean> filter = _expressionFactory.and(
        _stack.subList(_offset, _stack.size())
    );
  }

  @Override
  public void exitNegation(final NumberSelectionParser.@NonNull NegationContext context) {
    _stack.set(_stack.size() - 1, _expressionFactory.not(_stack.get(_stack.size() - 1)));
  }

  @Override
  public void exitNear(final NumberSelectionParser.@NonNull NearContext context) {
    @NonNull final Value delta = parse(context.delta);
    @NonNull final Value target = parse(context.target);

    _stack.add(
        _expressionFactory.between(
            _value,
            _expressionFactory.constant(_type, subtract(target, delta)),
            _expressionFactory.constant(_type, add(target, delta))
        )
    );
  }

  @Override
  public void exitOperation(final NumberSelectionParser.@NonNull OperationContext context) {
    @NonNull final Expression<Boolean> result;
    @NonNull final Expression<Value> compared = parseExpression(context.target);

    switch (context.name == null ? NumberSelectionLexer.EQUAL : context.name.getType()) {
      case NumberSelectionLexer.GREATHER_THAN:
        result = _expressionFactory.greaterThan(_value, compared);
        break;
      case NumberSelectionLexer.GREATHER_THAN_OR_EQUAL:
        result = _expressionFactory.greaterThanOrEqual(_value, compared);
        break;
      case NumberSelectionLexer.LESS_THAN:
        result = _expressionFactory.lessThan(_value, compared);
        break;
      case NumberSelectionLexer.LESS_THAN_OR_EQUAL:
        result = _expressionFactory.lessThanOrEqual(_value, compared);
        break;
      default:
        result = _expressionFactory.equal(_value, compared);
        break;
    }

    _stack.add(result);
  }

  @Override
  public void exitRange(final NumberSelectionParser.@NonNull RangeContext context) {
    @NonNull final Value left = parse(context.left);
    @NonNull final Value right = parse(context.right);

    _stack.add(_expressionFactory.between(
        _value,
        _expressionFactory.constant(_type, Utils.min(left, right)),
        _expressionFactory.constant(_type, Utils.max(left, right))
    ));
  }

  /**
   * @see SelectionToExpressionCompiler#transpile(CharSequence)
   */
  @Override
  public @NonNull Expression<@NonNull Boolean> transpile(@NonNull final CharSequence expression) {
    @NonNull final NumberSelectionLexer lexer = (
        new NumberSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    @NonNull final NumberSelectionParser parser = (
        new NumberSelectionParser(new CommonTokenStream(lexer))
    );

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _stack.get(0);
  }

  /**
   * @see SelectionToExpressionCompiler#tryToTranspile(CharSequence)
   */
  public @NonNull Expression<@NonNull Boolean> tryToTranspile(
      @NonNull final CharSequence expression
  ) throws TranspilationException {
    @NonNull final NumberSelectionLexer lexer = (
        new NumberSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

    @NonNull final NumberSelectionParser parser = (
        new NumberSelectionParser(new CommonTokenStream(lexer))
    );

    parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _stack.get(0);
  }

  private @NonNull Expression<Value> parseExpression(@NonNull final Token token) {
    return _expressionFactory.constant(_type, parse(token));
  }

  protected abstract @NonNull Value parse(@NonNull final Token token);

  protected abstract @NonNull Value add(@NonNull final Value left, @NonNull final Value right);

  protected abstract @NonNull Value subtract(
      @NonNull final Value left,
      @NonNull final Value right
  );
}
