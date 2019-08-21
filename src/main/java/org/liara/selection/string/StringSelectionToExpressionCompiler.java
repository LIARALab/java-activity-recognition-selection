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

package org.liara.selection.string;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.data.primitive.Primitives;
import org.liara.expression.Expression;
import org.liara.expression.ExpressionFactory;
import org.liara.selection.SelectionToExpressionCompiler;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.TranspilationException;
import org.liara.selection.antlr.StringSelectionBaseListener;
import org.liara.selection.antlr.StringSelectionLexer;
import org.liara.selection.antlr.StringSelectionParser;

public class StringSelectionToExpressionCompiler
    extends StringSelectionBaseListener
    implements SelectionToExpressionCompiler {

  @NonNull
  private final ExpressionFactory _expressionFactory;

  @NonNull
  private final List<@NonNull Expression<@NonNull Boolean>> _stack;

  @NonNegative
  private int _offset;

  @NonNull
  private Expression<@NonNull String> _value;

  public StringSelectionToExpressionCompiler() {
    _stack = new ArrayList<>(20);
    _offset = 0;
    _expressionFactory = new ExpressionFactory();
    _value = _expressionFactory.placeholder(Primitives.STRING);
  }

  /**
   * @see StringSelectionBaseListener#enterSelection(StringSelectionParser.SelectionContext)
   */
  @Override
  public void enterSelection(final StringSelectionParser.@NonNull SelectionContext context) {
    _stack.clear();
    _offset = 0;
  }

  /**
   * @see StringSelectionBaseListener#exitSelection(StringSelectionParser.SelectionContext)
   */
  @Override
  public void exitSelection(final StringSelectionParser.@NonNull SelectionContext context) {
    @NonNull final Expression<@NonNull Boolean> selection = _expressionFactory.or(_stack);
    _stack.clear();
    _stack.add(selection);
  }

  /**
   * @see StringSelectionBaseListener#enterFilter(StringSelectionParser.FilterContext)
   */
  @Override
  public void enterFilter(final StringSelectionParser.@NonNull FilterContext context) {
    _offset = _stack.size();
  }

  /**
   * @see StringSelectionBaseListener#exitFilter(StringSelectionParser.FilterContext)
   */
  @Override
  public void exitFilter(final StringSelectionParser.@NonNull FilterContext context) {
    @NonNull final Expression<@NonNull Boolean> filter = _expressionFactory.and(
        _stack.subList(_offset, _stack.size())
    );
  }

  /**
   * @see StringSelectionBaseListener#exitNegation(StringSelectionParser.NegationContext)
   */
  @Override
  public void exitNegation(final StringSelectionParser.@NonNull NegationContext context) {
    _stack.set(_stack.size() - 1, _expressionFactory.not(_stack.get(_stack.size() - 1)));
  }

  /**
   * @see StringSelectionBaseListener#exitOperation(StringSelectionParser.OperationContext)
   */
  @Override
  public void exitOperation(final StringSelectionParser.@NonNull OperationContext context) {
    if (context.STRING() != null) {
      exitString(context.STRING().getText());
    } else if (context.REGEXP() != null) {
      exitRegexp(context.REGEXP().getText());
    } else if (context.TOKEN() != null) {
      exitToken(context.TOKEN().getText());
    }
  }

  private void exitToken(@NonNull final String text) {
    @NonNull String content = text;
    boolean exact = false;

    while (content.startsWith("not:") || content.startsWith("eq:")) {
      if (content.startsWith("not:")) {
        content = content.substring(4);
        _stack.set(_stack.size() - 1, _expressionFactory.not(_stack.get(_stack.size() - 1)));
      } else {
        exact = true;
        content = content.substring(3);
      }
    }

    if (exact) {
      _stack.add(
          _expressionFactory.equal(
              _value,
              _expressionFactory.nonNullConstant(content)
          )
      );
    } else {
      _stack.add(
          _expressionFactory.like(
              _value,
              _expressionFactory.nonNullConstant("%" + content + "%")
          )
      );
    }
  }

  private void exitRegexp(@NonNull final String expression) {
    @NonNull final String content = (
        expression.substring(1, expression.length() - 1).replaceAll("\\\\/", "/")
    );

    _stack.add(
        _expressionFactory.regexp(
            _value,
            _expressionFactory.nonNullConstant(content)
        )
    );
  }

  private void exitString(@NonNull final String expression) {
    @NonNull final String content = (
        expression.substring(1, expression.length() - 1).replaceAll("\\\\\"", "\"")
    );

    _stack.add(
        _expressionFactory.like(
            _value,
            _expressionFactory.nonNullConstant("%" + content + "%")
        )
    );
  }

  /**
   * @see SelectionToExpressionCompiler#transpile(CharSequence)
   */
  @Override
  public @NonNull Expression<@NonNull Boolean> transpile(@NonNull final CharSequence expression) {
    @NonNull final StringSelectionLexer lexer = (
        new StringSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    @NonNull final StringSelectionParser parser = (
        new StringSelectionParser(new CommonTokenStream(lexer))
    );

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _stack.get(0);
  }

  /**
   * @see SelectionToExpressionCompiler#tryToTranspile(CharSequence)
   */
  @Override
  public @NonNull Expression<@NonNull Boolean> tryToTranspile(
      @NonNull final CharSequence expression
  ) throws TranspilationException {
    @NonNull final StringSelectionLexer lexer = (
        new StringSelectionLexer(CharStreams.fromString(expression.toString()))
    );
    lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

    @NonNull final StringSelectionParser parser = (
        new StringSelectionParser(new CommonTokenStream(lexer))
    );
    parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _stack.get(0);
  }
}
