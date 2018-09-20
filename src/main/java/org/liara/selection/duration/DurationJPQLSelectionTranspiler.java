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
package org.liara.selection.duration;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.selection.Utils;
import org.liara.selection.antlr.DurationSelectionBaseListener;
import org.liara.selection.antlr.DurationSelectionLexer;
import org.liara.selection.antlr.DurationSelectionParser;
import org.liara.selection.jpql.JPQLClauseBuilder;
import org.liara.selection.jpql.JPQLQuery;
import org.liara.selection.jpql.JPQLQueryBuilder;
import org.liara.selection.jpql.JPQLSelectionTranspiler;

import java.time.Duration;
import java.util.List;

public class DurationJPQLSelectionTranspiler
  extends DurationSelectionBaseListener
  implements JPQLSelectionTranspiler
{
  @NonNull
  private final JPQLClauseBuilder _currentClause;

  @NonNull
  private final JPQLQueryBuilder _currentSelection;

  public DurationJPQLSelectionTranspiler () {
    _currentClause = new JPQLClauseBuilder();
    _currentSelection = new JPQLQueryBuilder();
  }

  @Override
  public void enterSelection (
    final DurationSelectionParser.@NonNull SelectionContext context
  )
  { _currentSelection.reset(); }

  @Override
  public void exitFilter (
    final DurationSelectionParser.@NonNull FilterContext context
  )
  { _currentSelection.appendFilter(); }

  @Override
  public void enterClause (
    final DurationSelectionParser.@NonNull ClauseContext context
  )
  { _currentClause.reset(); }

  @Override
  public void exitClause (
    final DurationSelectionParser.@NonNull ClauseContext context
  )
  { _currentSelection.appendClause(_currentClause); }

  @Override
  public void exitNegation (
    final DurationSelectionParser.@NonNull NegationContext context
  )
  { _currentClause.negate(); }

  @Override
  public void exitNear (
    final DurationSelectionParser.@NonNull NearContext context
  )
  {
    @NonNull final Duration delta  = parseDuration(context.delta);
    @NonNull final Duration target = parseDuration(context.target);

    _currentClause.appendSelf();
    _currentClause.appendLiteral("BETWEEN");
    _currentClause.appendParameter(
      "min",
      toMilliseconds(target.minus(delta))
    );
    _currentClause.appendLiteral("AND");
    _currentClause.appendParameter(
      "max",
      toMilliseconds(target.plus(delta))
    );
  }

  @Override
  public void exitOperation (
    final DurationSelectionParser.@NonNull OperationContext context
  )
  {
    @NonNull final String operator;
    final int             operatorType = (context.name == null) ? DurationSelectionLexer.EQUAL : context.name.getType();

    _currentClause.appendSelf();

    switch (operatorType) {
      case DurationSelectionLexer.GREATHER_THAN:
        operator = ">";
        break;
      case DurationSelectionLexer.GREATHER_THAN_OR_EQUAL:
        operator = ">=";
        break;
      case DurationSelectionLexer.LESS_THAN:
        operator = "<";
        break;
      case DurationSelectionLexer.LESS_THAN_OR_EQUAL:
        operator = "<=";
        break;
      default:
        operator = "=";
        break;
    }

    _currentClause.appendLiteral(operator);
    _currentClause.appendParameter(
      "value",
      toMilliseconds(parseDuration(context.duration()))
    );
  }

  @Override
  public void exitRange (
    final DurationSelectionParser.@NonNull RangeContext context
  )
  {
    @NonNull final Duration left  = parseDuration(context.left);
    @NonNull final Duration right = parseDuration(context.right);

    _currentClause.appendSelf();
    _currentClause.appendLiteral("BETWEEN");
    _currentClause.appendParameter(
      "min",
      toMilliseconds(Utils.min(
        left,
        right
      ))
    );
    _currentClause.appendLiteral("AND");
    _currentClause.appendParameter(
      "max",
      toMilliseconds(Utils.max(
        left,
        right
      ))
    );
  }

  private @NonNull Long toMilliseconds (@NonNull final Duration duration) {
    return duration.toMillis();
  }

  private @NonNull Duration parseDuration (final DurationSelectionParser.@NonNull DurationContext duration) {
    @NonNull Duration                                                          result  = Duration.ofMillis(0);
    @NonNull final List<DurationSelectionParser.@NonNull DurationEntryContext> entries = duration.durationEntry();

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

  protected @NonNull Long parse (@NonNull final Token token) {
    try {
      return Long.parseLong(token.getText());
    } catch (@NonNull final NumberFormatException exception) {
      throw new Error(
        String.join("",
                    "Invalid number format at line ",
                    String.valueOf(token.getLine()),
                    " and index ",
                    String.valueOf(token.getCharPositionInLine()),
                    " : \"",
                    token.getText(),
                    "\""
        ),
        exception
      );
    }
  }

  public @NonNull JPQLQuery transpile (@NonNull final CharSequence expression) {
    @NonNull final DurationSelectionLexer lexer = new DurationSelectionLexer(CharStreams.fromString(expression.toString()));

    @NonNull final DurationSelectionParser parser = new DurationSelectionParser(new CommonTokenStream(lexer));

    ParseTreeWalker.DEFAULT.walk(
      this,
      parser.selection()
    );

    return _currentSelection.build();
  }
}
