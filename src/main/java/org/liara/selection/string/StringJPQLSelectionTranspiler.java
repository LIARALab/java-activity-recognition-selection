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

package org.liara.selection.string;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.selection.antlr.StringSelectionBaseListener;
import org.liara.selection.antlr.StringSelectionLexer;
import org.liara.selection.antlr.StringSelectionParser;
import org.liara.selection.jpql.JPQLClauseBuilder;
import org.liara.selection.jpql.JPQLQuery;
import org.liara.selection.jpql.JPQLQueryBuilder;
import org.liara.selection.jpql.JPQLSelectionTranspiler;

public class StringJPQLSelectionTranspiler
  extends StringSelectionBaseListener
  implements JPQLSelectionTranspiler
{
  @NonNull
  private final JPQLClauseBuilder _currentClause;

  @NonNull
  private final JPQLQueryBuilder _currentSelection;

  public StringJPQLSelectionTranspiler () {
    _currentClause = new JPQLClauseBuilder();
    _currentSelection = new JPQLQueryBuilder();
  }

  @Override
  public void enterSelection (
    final StringSelectionParser.@NonNull SelectionContext context
  ) { _currentSelection.reset(); }

  @Override
  public void exitFilter (
    final StringSelectionParser.@NonNull FilterContext context
  ) { _currentSelection.appendFilter(); }

  @Override
  public void enterClause (
    final StringSelectionParser.@NonNull ClauseContext context
  ) { _currentClause.reset(); }

  @Override
  public void exitClause (
    final StringSelectionParser.@NonNull ClauseContext context
  ) { _currentSelection.appendClause(_currentClause); }

  @Override
  public void exitNegation (
    final StringSelectionParser.@NonNull NegationContext context
  ) { _currentClause.negate(); }

  @Override
  public void exitOperation (final StringSelectionParser.@NonNull OperationContext context) {
    if (context.STRING() != null) {
      exitString(context.STRING().getText());
    } else if (context.REGEXP() != null) {
      exitRegexp(context.REGEXP().getText());
    } else if (context.TOKEN() != null) {
      exitToken(context.TOKEN().getText());
    }
  }

  private void exitToken (@NonNull final String text) {
    @NonNull final String content;

    if (text.startsWith("not:")) {
      content = text.substring(4);
      _currentClause.negate();
    } else {
      content = text;
    }

    _currentClause.appendSelf();
    _currentClause.appendLiteral("LIKE");
    _currentClause.appendParameter("keyword", "%" + content + "%");
  }

  private void exitRegexp (@NonNull final String expression) {
    @NonNull final String content = expression.substring(1, expression.length() - 1)
                                              .replaceAll("\\\\/", "/");

    _currentClause.appendSelf();
    _currentClause.appendLiteral("REGEXP");
    _currentClause.appendParameter("expression", content);
  }

  private void exitString (@NonNull final String expression) {
    @NonNull final String content = expression.substring(1, expression.length() - 1)
                                              .replaceAll("\\\\\"", "\"");
    _currentClause.appendSelf();
    _currentClause.appendLiteral("LIKE");
    _currentClause.appendParameter("keyword","%" + content + "%");
  }

  public @NonNull JPQLQuery transpile (@NonNull final CharSequence expression) {
    @NonNull final StringSelectionLexer lexer = new StringSelectionLexer(CharStreams.fromString(expression.toString()));

    @NonNull final StringSelectionParser parser = new StringSelectionParser(new CommonTokenStream(lexer));

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _currentSelection.build();
  }
}