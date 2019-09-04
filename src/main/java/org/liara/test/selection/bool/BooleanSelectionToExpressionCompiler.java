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

package org.liara.test.selection.bool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
import org.liara.selection.CompilationException;
import org.liara.selection.SelectionToExpressionCompiler;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.antlr.BooleanSelectionBaseListener;
import org.liara.selection.antlr.BooleanSelectionLexer;
import org.liara.selection.antlr.BooleanSelectionParser;
import org.liara.selection.antlr.BooleanSelectionParser.FilterContext;
import org.liara.selection.antlr.BooleanSelectionParser.NegationContext;
import org.liara.selection.antlr.BooleanSelectionParser.OperationContext;
import org.liara.selection.antlr.BooleanSelectionParser.SelectionContext;

/**
 * A compiler that transforms string selections into a full expression tree.
 */
public class BooleanSelectionToExpressionCompiler
    extends BooleanSelectionBaseListener
    implements SelectionToExpressionCompiler<@Nullable Boolean> {

  @NonNull
  private final ExpressionFactory _expressionFactory;
  @NonNull
  private final List<@NonNull Expression<@NonNull Boolean>> _stack;
  @NonNull
  private final BooleanSelectionLexer _lexer;
  @NonNull
  private final BooleanSelectionParser _parser;
  @NonNegative
  private int _offset;
  @NonNull
  private Expression<@Nullable Boolean> _filteredValue;

  /**
   * Create a new instance of a boolean selection to expression compiler.
   */
  public BooleanSelectionToExpressionCompiler() {
    _stack = new ArrayList<>(20);
    _offset = 0;
    _expressionFactory = new ExpressionFactory();
    _filteredValue = _expressionFactory.placeholder(Primitives.BOOLEAN);
    _lexer = new BooleanSelectionLexer(CharStreams.fromString(""));
    _parser = new BooleanSelectionParser(new CommonTokenStream(_lexer));
  }

  /**
   * @see BooleanSelectionBaseListener#enterSelection(SelectionContext)
   */
  @Override
  public void enterSelection(final BooleanSelectionParser.@NonNull SelectionContext context) {
    _stack.clear();
    _offset = 0;
  }

  /**
   * @see BooleanSelectionBaseListener#exitSelection(SelectionContext)
   */
  @Override
  public void exitSelection(final BooleanSelectionParser.@NonNull SelectionContext context) {
    @NonNull final Expression<@NonNull Boolean> selection = _expressionFactory.or(_stack);
    _stack.clear();
    _stack.add(selection);
  }

  /**
   * @see BooleanSelectionBaseListener#exitFilter(FilterContext)
   */
  @Override
  public void exitFilter(final BooleanSelectionParser.@NonNull FilterContext context) {
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> clauses = (
        _stack.subList(_offset, _stack.size())
    );

    @NonNull final Expression<@NonNull Boolean> filter = _expressionFactory.and(clauses);

    clauses.clear();

    _stack.add(filter);
    _offset += 1;
  }

  /**
   * @see BooleanSelectionBaseListener#exitNegation(NegationContext)
   */
  @Override
  public void exitNegation(final BooleanSelectionParser.@NonNull NegationContext context) {
    _stack.set(_stack.size() - 1, _expressionFactory.not(_stack.get(_stack.size() - 1)));
  }

  /**
   * @see BooleanSelectionBaseListener#exitOperation(OperationContext)
   */
  @Override
  public void exitOperation(final BooleanSelectionParser.@NonNull OperationContext context) {
    System.out.println(_stack.stream().map(Object::toString).collect(Collectors.joining(", ")));
    _stack.add(_expressionFactory.equal(_filteredValue, parse(context.target)));
  }

  /**
   * Parse a value token.
   *
   * @param target A value token.
   * @return True, false or null if the given token is respectively true, false, or null.
   */
  private @NonNull Expression<@Nullable Boolean> parse(@NonNull final Token target) {
    switch (target.getType()) {
      case BooleanSelectionLexer.NULL:
        return _expressionFactory.nullable((Boolean) null);
      case BooleanSelectionLexer.FALSE:
        return _expressionFactory.nonnull(false);
      default:
        return _expressionFactory.nonnull(true);
    }
  }

  /**
   * @see SelectionToExpressionCompiler#compile(CharSequence)
   */
  @Override
  public @NonNull Expression<@NonNull Boolean> compile(@NonNull final CharSequence selection) {
    if (selection.toString().trim().equalsIgnoreCase("")) {
      return _expressionFactory.equal(_filteredValue, _expressionFactory.nonnull(true));
    }

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
  public @NonNull Expression<@NonNull Boolean> tryToCompile(
      @NonNull final CharSequence selection
  ) throws CompilationException {
    if (selection.toString().trim().equalsIgnoreCase("")) {
      return _expressionFactory.equal(_filteredValue, _expressionFactory.nonnull(true));
    }

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
   * @return The filtered value.
   */
  public @NonNull Expression<@Nullable Boolean> getFilteredValue() {
    return _filteredValue;
  }

  /**
   * Update the expression of the value to filter.
   *
   * @param value The new expression of the value to filter.
   */
  public void setFilteredValue(@NonNull final Expression<@Nullable Boolean> value) {
    _filteredValue = value;
  }
}
