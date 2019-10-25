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

package org.liara.test.selection.string;

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
import org.liara.selection.CompilationException;
import org.liara.selection.SelectionToExpressionCompiler;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.antlr.StringSelectionBaseListener;
import org.liara.selection.antlr.StringSelectionLexer;
import org.liara.selection.antlr.StringSelectionParser;

public class StringSelectionToExpressionCompiler
    extends StringSelectionBaseListener
    implements SelectionToExpressionCompiler<@NonNull String> {

  @NonNull
  private final ExpressionFactory _expressionFactory;

  @NonNull
  private final List<@NonNull Expression<@NonNull Boolean>> _stack;
  @NonNull
  private final StringSelectionLexer _lexer;
  @NonNull
  private final StringSelectionParser _parser;
  @NonNegative
  private int _offset;
  @NonNull
  private Expression<@NonNull String> _filteredValue;

  public StringSelectionToExpressionCompiler() {
    _stack = new ArrayList<>(20);
    _offset = 0;
    _expressionFactory = new ExpressionFactory();
    _filteredValue = _expressionFactory.placeholder(Primitives.STRING);
    _lexer = new StringSelectionLexer(CharStreams.fromString(""));
    _parser = new StringSelectionParser(new CommonTokenStream(_lexer));
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
   * @see StringSelectionBaseListener#exitFilter(StringSelectionParser.FilterContext)
   */
  @Override
  public void exitFilter(final StringSelectionParser.@NonNull FilterContext context) {
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> clauses = _stack.subList(
        _offset, _stack.size()
    );

    @NonNull final Expression<@NonNull Boolean> filter = _expressionFactory.and(clauses);

    clauses.clear();

    _stack.add(filter);
    _offset += 1;
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
    int negations = 0;

    while (content.startsWith("not:") || content.startsWith("eq:")) {
      if (content.startsWith("not:")) {
        content = content.substring(4);
        negations += 1;
      } else {
        exact = true;
        content = content.substring(3);
      }
    }

    @NonNull Expression<@NonNull Boolean> expression;

    if (exact) {
      expression = _expressionFactory.equal(_filteredValue, _expressionFactory.nonnull(content));
    } else {
      expression = _expressionFactory.like(_filteredValue, _expressionFactory.nonnull(
          "%" + content + "%"
      ));
    }

    for (int index = 0; index < negations; ++index) {
      expression = _expressionFactory.not(expression);
    }

    _stack.add(expression);
  }

  private void exitRegexp(@NonNull final String expression) {
    @NonNull final String content = (
        expression.substring(1, expression.length() - 1).replaceAll("\\\\/", "/")
    );

    _stack.add(
        _expressionFactory.regexp(
            _filteredValue,
            _expressionFactory.nonnull(content)
        )
    );
  }

  private void exitString(@NonNull final String expression) {
    @NonNull final String content = (
        expression.substring(1, expression.length() - 1).replaceAll("\\\\\"", "\"")
    );

    _stack.add(
        _expressionFactory.like(
            _filteredValue,
            _expressionFactory.nonnull("%" + content + "%")
        )
    );
  }

  /**
   * @see SelectionToExpressionCompiler#compile(CharSequence)
   */
  @Override
  public @NonNull Expression<@NonNull Boolean> compile(@NonNull final CharSequence selection) {
    _lexer.setInputStream(CharStreams.fromString(selection.toString()));
    _lexer.reset();

    _parser.setTokenStream(new CommonTokenStream(_lexer));
    _parser.reset();

    ParseTreeWalker.DEFAULT.walk(this, _parser.selection());

    return _stack.get(0);
  }

  /**
   * @see SelectionToExpressionCompiler#tryToCompile(CharSequence)
   */
  @Override
  public @NonNull Expression<@NonNull Boolean> tryToCompile(@NonNull final CharSequence selection)
      throws CompilationException {
    _lexer.setInputStream(CharStreams.fromString(selection.toString()));
    _lexer.reset();

    _parser.setTokenStream(new CommonTokenStream(_lexer));
    _parser.reset();

    _lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
    _parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, _parser.selection());

    return _stack.get(0);
  }

  /**
   * @return The filtered value expression.
   */
  public @NonNull Expression<@NonNull String> getFilteredValue() {
    return _filteredValue;
  }

  /**
   * Update the expression of the value to filter.
   *
   * @param value The new expression of the value to filter.
   */
  public void setFilteredValue(Expression<@NonNull String> value) {
    _filteredValue = value;
  }
}
