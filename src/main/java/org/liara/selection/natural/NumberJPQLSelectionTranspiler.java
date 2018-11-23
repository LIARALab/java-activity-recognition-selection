/*
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.TranspilationException;
import org.liara.selection.Utils;
import org.liara.selection.antlr.NumberSelectionBaseListener;
import org.liara.selection.antlr.NumberSelectionLexer;
import org.liara.selection.antlr.NumberSelectionParser;
import org.liara.selection.jpql.JPQLClauseBuilder;
import org.liara.selection.jpql.JPQLQuery;
import org.liara.selection.jpql.JPQLQueryBuilder;
import org.liara.selection.jpql.JPQLSelectionTranspiler;

public abstract class NumberJPQLSelectionTranspiler<Value extends Comparable<Value>>
  extends NumberSelectionBaseListener
  implements JPQLSelectionTranspiler
{
  @NonNull
  private final JPQLClauseBuilder _currentClause;

  @NonNull
  private final JPQLQueryBuilder _currentSelection;

  public NumberJPQLSelectionTranspiler () {
    _currentClause = new JPQLClauseBuilder();
    _currentSelection = new JPQLQueryBuilder();
  }

  @Override
  public void enterSelection (
    final NumberSelectionParser.@NonNull SelectionContext context
  ) { _currentSelection.reset(); }

  @Override
  public void exitFilter (
    final NumberSelectionParser.@NonNull FilterContext context
  ) { _currentSelection.appendFilter(); }

  @Override
  public void enterClause (
    final NumberSelectionParser.@NonNull ClauseContext context
  ) { _currentClause.reset(); }

  @Override
  public void exitClause (
    final NumberSelectionParser.@NonNull ClauseContext context
  ) { _currentSelection.appendClause(_currentClause); }

  @Override
  public void exitNegation (
    final NumberSelectionParser.@NonNull NegationContext context
  ) { _currentClause.negate(); }

  @Override
  public void exitNear (
    final NumberSelectionParser.@NonNull NearContext context
  ) {
    @NonNull final Value delta  = parse(context.delta);
    @NonNull final Value target = parse(context.target);

    _currentClause.appendSelf();
    _currentClause.appendLiteral("BETWEEN");
    _currentClause.appendParameter("min", this.subtract(target, delta));
    _currentClause.appendLiteral("AND");
    _currentClause.appendParameter("max", this.add(target, delta));
  }

  @Override
  public void exitOperation (
    final NumberSelectionParser.@NonNull OperationContext context
  ) {
    @NonNull final String operator;
    final int             operatorType = (context.name == null) ? NumberSelectionLexer.EQUAL : context.name.getType();

    _currentClause.appendSelf();

    switch(operatorType) {
      case NumberSelectionLexer.GREATHER_THAN: operator = ">"; break;
      case NumberSelectionLexer.GREATHER_THAN_OR_EQUAL: operator = ">="; break;
      case NumberSelectionLexer.LESS_THAN: operator = "<"; break;
      case NumberSelectionLexer.LESS_THAN_OR_EQUAL: operator = "<="; break;
      default: operator = "="; break;
    }

    _currentClause.appendLiteral(operator);
    _currentClause.appendParameter("value", parse(context.target));
  }

  @Override
  public void exitRange (
    final NumberSelectionParser.@NonNull RangeContext context
  ) {
    @NonNull final Value left  = parse(context.left);
    @NonNull final Value right = parse(context.right);

    _currentClause.appendSelf();
    _currentClause.appendLiteral("BETWEEN");
    _currentClause.appendParameter("min", Utils.min(left, right));
    _currentClause.appendLiteral("AND");
    _currentClause.appendParameter("max", Utils.max(left, right));
  }

  public @NonNull JPQLQuery transpile (@NonNull final CharSequence expression) {
    @NonNull final NumberSelectionLexer lexer = new NumberSelectionLexer(CharStreams.fromString(expression.toString()));

    @NonNull final NumberSelectionParser parser = new NumberSelectionParser(new CommonTokenStream(lexer));

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _currentSelection.build();
  }

  public @NonNull JPQLQuery tryToTranspile (@NonNull final CharSequence expression)
  throws TranspilationException
  {
    @NonNull final NumberSelectionLexer lexer = new NumberSelectionLexer(CharStreams.fromString(expression.toString()));
    lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
    @NonNull final NumberSelectionParser parser = new NumberSelectionParser(new CommonTokenStream(lexer));
    parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _currentSelection.build();
  }

  protected abstract @NonNull Value parse (@NonNull final Token token);

  protected abstract @NonNull Value add(@NonNull final Value left, @NonNull final Value right);

  protected abstract @NonNull Value subtract(@NonNull final Value left, @NonNull final Value right);
}
