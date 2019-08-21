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
package org.liara.selection.duration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.data.primitive.Primitives;
import org.liara.expression.Expression;
import org.liara.expression.ExpressionFactory;
import org.liara.selection.SelectionToExpressionCompiler;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.TranspilationException;
import org.liara.selection.Utils;
import org.liara.selection.antlr.DurationSelectionBaseListener;
import org.liara.selection.antlr.DurationSelectionLexer;
import org.liara.selection.antlr.DurationSelectionParser;

public class DurationSelectionToExpressionCompiler
    extends DurationSelectionBaseListener
    implements SelectionToExpressionCompiler {

  @NonNull
  private final ExpressionFactory _expressionFactory;

  @NonNull
  private final List<@NonNull Expression<@NonNull Boolean>> _stack;

  @NonNegative
  private int _offset;

  @NonNull
  private Expression<@NonNull Long> _value;

  public DurationSelectionToExpressionCompiler() {
    _stack = new ArrayList<>(20);
    _offset = 0;
    _expressionFactory = new ExpressionFactory();
    _value = _expressionFactory.placeholder(Primitives.LONG);
  }

  @Override
  public void enterSelection(
      final DurationSelectionParser.@NonNull SelectionContext context
  ) {
    _stack.clear();
    _offset = 0;
  }

  @Override
  public void exitSelection(final DurationSelectionParser.@NonNull SelectionContext context) {
    @NonNull final Expression<@NonNull Boolean> selection = _expressionFactory.or(_stack);
    _stack.clear();
    _stack.add(selection);
  }

  @Override
  public void enterFilter(final DurationSelectionParser.@NonNull FilterContext context) {
    _offset = _stack.size();
  }

  @Override
  public void exitFilter(
      final DurationSelectionParser.@NonNull FilterContext context
  ) {
    @NonNull final Expression<@NonNull Boolean> filter = _expressionFactory.and(
        _stack.subList(_offset, _stack.size())
    );
  }

  @Override
  public void exitNegation(
      final DurationSelectionParser.@NonNull NegationContext context
  ) {
    _stack.set(_stack.size() - 1, _expressionFactory.not(_stack.get(_stack.size() - 1)));
  }

  @Override
  public void exitNear(final DurationSelectionParser.@NonNull NearContext context) {
    @NonNull final Duration delta = parseDuration(context.delta);
    @NonNull final Duration target = parseDuration(context.target);

    _stack.add(
        _expressionFactory.between(
            _value,
            _expressionFactory.nonnull(toMilliseconds(target.minus(delta))),
            _expressionFactory.nonnull(toMilliseconds(target.plus(delta)))
        )
    );
  }

  @Override
  public void exitOperation(final DurationSelectionParser.@NonNull OperationContext context) {
    @NonNull final Expression<@NonNull Boolean> expression;
    @NonNull final Expression<@NonNull Long> duration = _expressionFactory.nonnull(
        toMilliseconds(parseDuration(context.duration()))
    );

    switch (context.name == null ? DurationSelectionLexer.EQUAL : context.name.getType()) {
      case DurationSelectionLexer.GREATHER_THAN:
        expression = _expressionFactory.greaterThan(_value, duration);
        break;
      case DurationSelectionLexer.GREATHER_THAN_OR_EQUAL:
        expression = _expressionFactory.greaterThanOrEqual(_value, duration);
        break;
      case DurationSelectionLexer.LESS_THAN:
        expression = _expressionFactory.lessThan(_value, duration);
        break;
      case DurationSelectionLexer.LESS_THAN_OR_EQUAL:
        expression = _expressionFactory.lessThanOrEqual(_value, duration);
        break;
      default:
        expression = _expressionFactory.equal(_value, duration);
        break;
    }

    _stack.add(expression);
  }

  @Override
  public void exitRange(final DurationSelectionParser.@NonNull RangeContext context) {
    @NonNull final Duration left = parseDuration(context.left);
    @NonNull final Duration right = parseDuration(context.right);

    _stack.add(
        _expressionFactory.between(
            _value,
            _expressionFactory.nonnull(toMilliseconds(Utils.min(left, right))),
            _expressionFactory.nonnull(toMilliseconds(Utils.max(left, right)))
        )
    );
  }

  private @NonNull Long toMilliseconds(@NonNull final Duration duration) {
    return duration.toMillis();
  }

  private @NonNull Duration parseDuration(
      final DurationSelectionParser.@NonNull DurationContext duration) {
    @NonNull Duration result =
        Duration.ofMillis(
            0);
    @NonNull final List<DurationSelectionParser.@NonNull DurationEntryContext> entries =
        duration.durationEntry();

    for (final DurationSelectionParser.@NonNull DurationEntryContext entry : entries) {
      final long quantity = parse(entry.value);
      switch (entry.unit.getType()) {
        case DurationSelectionLexer.YEAR:
          result = result.plusDays(quantity * 365);
          break;
        case DurationSelectionLexer.MONTH:
          result = result.plusDays(quantity * 30);
          break;
        case DurationSelectionLexer.WEEK:
          result = result.plusDays(quantity * 7);
          break;
        case DurationSelectionLexer.DAY:
          result = result.plusDays(
              quantity
          );
          break;
        case DurationSelectionLexer.HOUR:
          result = result.plusHours(
              quantity
          );
          break;
        case DurationSelectionLexer.MINUTE:
          result = result.plusMinutes(
              quantity
          );
          break;
        case DurationSelectionLexer.SECOND:
          result = result.plusSeconds(
              quantity
          );
          break;
        case DurationSelectionLexer.MILLISECOND:
          result = result.plusMillis(
              quantity
          );
          break;
      }
    }

    return result;
  }

  protected @NonNull Long parse(@NonNull final Token token) {
    try {
      return Long.parseLong(token.getText());
    } catch (@NonNull final NumberFormatException exception) {
      throw new Error(
          "Invalid number format at line " + String.valueOf(token.getLine()) + " and index " +
              String.valueOf(token.getCharPositionInLine()) + " : \"" + token.getText() + "\"",
        exception
      );
    }
  }

  public @NonNull Expression<@NonNull Boolean> transpile(@NonNull final CharSequence expression) {
    @NonNull final DurationSelectionLexer lexer = new DurationSelectionLexer(
        CharStreams.fromString(expression.toString())
    );

    @NonNull final DurationSelectionParser parser = new DurationSelectionParser(
        new CommonTokenStream(lexer)
    );

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _stack.get(0);
  }

  public @NonNull Expression<@NonNull Boolean> tryToTranspile(
      @NonNull final CharSequence expression
  ) throws TranspilationException {
    @NonNull final DurationSelectionLexer lexer = new DurationSelectionLexer(
        CharStreams.fromString(expression.toString())
    );

    lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

    @NonNull final DurationSelectionParser parser = new DurationSelectionParser(
        new CommonTokenStream(lexer)
    );

    parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _stack.get(0);
  }

  public @NonNull Expression<@NonNull Long> getValue() {
    return _value;
  }

  public void setValue(@NonNull final Expression<@NonNull Long> value) {
    _value = value;
  }
}
