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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.TranspilationException;
import org.liara.selection.antlr.BooleanSelectionBaseListener;
import org.liara.selection.antlr.BooleanSelectionLexer;
import org.liara.selection.antlr.BooleanSelectionParser;
import org.liara.selection.jpql.JPQLClauseBuilder;
import org.liara.selection.jpql.JPQLQuery;
import org.liara.selection.jpql.JPQLQueryBuilder;
import org.liara.selection.jpql.JPQLSelectionTranspiler;

public class BooleanJPQLSelectionTranspiler
  extends BooleanSelectionBaseListener
  implements JPQLSelectionTranspiler
{
  @NonNull
  private final JPQLClauseBuilder _currentClause;

  @NonNull
  private final JPQLQueryBuilder _currentSelection;

  public BooleanJPQLSelectionTranspiler () {
    _currentClause = new JPQLClauseBuilder();
    _currentSelection = new JPQLQueryBuilder();
  }

  @Override
  public void enterSelection (
    final BooleanSelectionParser.@NonNull SelectionContext context
  ) { _currentSelection.reset(); }

  @Override
  public void exitFilter (
    final BooleanSelectionParser.@NonNull FilterContext context
  ) { _currentSelection.appendFilter(); }

  @Override
  public void enterClause (
    final BooleanSelectionParser.@NonNull ClauseContext context
  ) { _currentClause.reset(); }

  @Override
  public void exitClause (
    final BooleanSelectionParser.@NonNull ClauseContext context
  ) { _currentSelection.appendClause(_currentClause); }

  @Override
  public void exitNegation (
    final BooleanSelectionParser.@NonNull NegationContext context
  ) { _currentClause.negate(); }

  @Override
  public void exitOperation (
    final BooleanSelectionParser.@NonNull OperationContext context
  ) {
    _currentClause.appendSelf();
    _currentClause.appendLiteral("=");
    _currentClause.appendParameter("value", parse(context.target));
  }

  private boolean parse (@NonNull final Token target) {
    return target.getType() == BooleanSelectionLexer.TRUE;
  }

  public @NonNull JPQLQuery transpile (@NonNull final CharSequence expression) {
    @NonNull final BooleanSelectionLexer lexer = (
      new BooleanSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    @NonNull final BooleanSelectionParser parser = (
      new BooleanSelectionParser(new CommonTokenStream(lexer))
    );

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _currentSelection.build();
  }

  public @NonNull JPQLQuery tryToTranspile (@NonNull final CharSequence expression)
  throws TranspilationException {
    @NonNull final BooleanSelectionLexer lexer = (
      new BooleanSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    @NonNull final BooleanSelectionParser parser = (
      new BooleanSelectionParser(new CommonTokenStream(lexer))
    );

    lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
    parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _currentSelection.build();
  }
}
