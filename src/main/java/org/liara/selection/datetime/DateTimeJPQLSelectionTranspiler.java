/*******************************************************************************
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.liara.selection.datetime;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.selection.Utils;
import org.liara.selection.antlr.*;
import org.liara.selection.jpql.JPQLClauseBuilder;
import org.liara.selection.jpql.JPQLQuery;
import org.liara.selection.jpql.JPQLQueryBuilder;
import org.liara.selection.jpql.JPQLSelectionTranspiler;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;

public class DateTimeJPQLSelectionTranspiler
  extends DateSelectionBaseListener
  implements JPQLSelectionTranspiler
{
  @NonNull
  private final JPQLClauseBuilder _currentClause;

  @NonNull
  private final JPQLQueryBuilder _currentSelection;

  @NonNull
  private Locale _defaultLocale;

  @NonNull
  private DateTimeFormatter _defaultFormat;

  public DateTimeJPQLSelectionTranspiler () {
    _currentClause = new JPQLClauseBuilder();
    _currentSelection = new JPQLQueryBuilder();
    _defaultLocale = Locale.getDefault();
    _defaultFormat = DateTimeFormatter.ISO_ZONED_DATE_TIME;
  }

  @Override
  public void enterSelection (
    final DateSelectionParser.@NonNull SelectionContext context
  )
  {
    _defaultLocale = Locale.getDefault();
    _defaultFormat = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    _currentSelection.reset();
  }

  @Override
  public void exitFilter (
    final DateSelectionParser.@NonNull FilterContext context
  )
  {
    _currentSelection.appendFilter();
  }

  @Override
  public void exitDefaultConfiguration (
    final DateSelectionParser.@NonNull DefaultConfigurationContext context
  )
  {
    _defaultLocale = getLocaleFrom(context.locale());
    _defaultFormat = getFormatFrom(
      context.format(),
      _defaultLocale
    );
  }

  @Override
  public void enterClause (
    final DateSelectionParser.@NonNull ClauseContext context
  )
  { _currentClause.reset(); }

  @Override
  public void exitClause (
    final DateSelectionParser.@NonNull ClauseContext context
  )
  { _currentSelection.appendClause(_currentClause); }

  @Override
  public void exitNegation (
    final DateSelectionParser.@NonNull NegationContext context
  )
  { _currentClause.negate(); }

  @Override
  public void exitOperation (
    final DateSelectionParser.@NonNull OperationContext context
  )
  {
    @NonNull final PartialZonedDateTime date         = parseDate(context.date());
    @NonNull final String               operator;
    final int                           operatorType =
      (context.name == null) ? DateSelectionLexer.EQUAL : context.name.getType();

    switch (operatorType) {
      case DateSelectionLexer.GREATHER_THAN:
        operator = ">";
        break;
      case DateSelectionLexer.GREATHER_THAN_OR_EQUAL:
        operator = ">=";
        break;
      case DateSelectionLexer.LESS_THAN:
        operator = "<";
        break;
      case DateSelectionLexer.LESS_THAN_OR_EQUAL:
        operator = "<=";
        break;
      default:
        operator = "=";
        break;
    }

    if (date.containsDatetime()) {
      date.appendMaskedDateTo(_currentClause);
      _currentClause.appendLiteral(operator);
      _currentClause.appendParameter(
        "value",
        date.toZonedDateTime()
      );
      if (date.containsContext()) {
        _currentClause.appendLiteral("AND");
      }
    }

    if (date.containsContext()) {
      boolean first = true;
      for (@NonNull final ChronoField field : PartialZonedDateTime.CONTEXT_FIELDS) {
        if (first) first = false;
        else _currentClause.appendLiteral("AND");

        date.appendField(
          _currentClause,
          field
        );
        _currentClause.appendLiteral(operator);
        _currentClause.appendParameter(
          field.toString()
               .toLowerCase() + "_value",
          date.getLong(field)
        );
      }
    }
  }

  @Override
  public void exitRange (
    final DateSelectionParser.@NonNull RangeContext context
  )
  {
    @NonNull final DateTimeFormatter    format = getFormatFrom(
      context.format(),
      getLocaleFrom(context.locale())
    );
    @NonNull final PartialZonedDateTime left   = format.parse(
      getTokenContent(context.left),
      PartialZonedDateTime::from
    );
    @NonNull final PartialZonedDateTime right = format.parse(
      getTokenContent(context.right),
      PartialZonedDateTime::from
    );

    appendBetween(
      left,
      right
    );
  }

  private void appendBetween (
    @NonNull final PartialZonedDateTime left,
    @NonNull final PartialZonedDateTime right
  )
  {
    if (left.containsDatetime()) {
      left.appendMaskedDateTo(_currentClause);
      _currentClause.appendLiteral("BETWEEN");
      _currentClause.appendParameter(
        "min",
        Utils.min(
          left,
          right
        )
             .toZonedDateTime()
      );
      _currentClause.appendLiteral("AND");
      _currentClause.appendParameter(
        "max",
        Utils.max(
          left,
          right
        )
             .toZonedDateTime()
      );
      if (left.containsContext()) {
        _currentClause.appendLiteral("AND");
      }
    }

    if (left.containsContext()) {
      boolean first = true;
      for (@NonNull final ChronoField field : PartialZonedDateTime.CONTEXT_FIELDS) {
        if (first) first = false;
        else _currentClause.appendLiteral("AND");

        left.appendField(
          _currentClause,
          field
        );
        _currentClause.appendLiteral("BETWEEN");
        _currentClause.appendParameter(
          "min",
          Utils.min(
            left,
            right
          )
               .getLong(field)
        );
        _currentClause.appendLiteral("AND");
        _currentClause.appendParameter(
          "max",
          Utils.max(
            left,
            right
          )
               .getLong(field)
        );
        if (left.containsContext()) {
          _currentClause.appendLiteral("AND");
        }
      }
    }
  }

  private @NonNull PartialZonedDateTime parseDate (final DateSelectionParser.@NonNull DateContext date) {
    try {
      @NonNull final DateTimeFormatter format = getFormatFrom(
        date.format(),
        getLocaleFrom(date.locale())
      );
      @NonNull final String            value  = getTokenContent(date.value);

      return format.parse(
        value,
        PartialZonedDateTime::from
      );
    } catch (@NonNull final Throwable exception) {
      throw new Error(
        String.join(
          "",
          "Invalid date at line ",
          String.valueOf(date.getStart()
                             .getLine()),
          " and index ",
          String.valueOf(date.getStart()
                             .getCharPositionInLine()),
          " : \"",
          date.getText(),
          "\""
        ),
        exception
      );
    }
  }

  private @NonNull DateTimeFormatter getFormatFrom (
    final DateSelectionParser.@Nullable FormatContext format,
    @NonNull final Locale locale
  )
  {
    return (format == null) ? _defaultFormat.withLocale(locale) : DateTimeFormatter.ofPattern(
      getTokenContent(format.TOKEN()),
      locale
    );
  }

  private @NonNull Locale getLocaleFrom (final DateSelectionParser.@Nullable LocaleContext locale) {
    return (locale == null) ? _defaultLocale : Locale.forLanguageTag(getTokenContent(locale.TOKEN()));
  }

  private @NonNull String getTokenContent (@NonNull final TerminalNode node) {
    @NonNull final String text = node.getText();
    return text.substring(
      1,
      text.length() - 1
    )
               .replaceAll(
                 "\\\\\\(",
                 "("
               );
  }

  private @NonNull String getTokenContent (@NonNull final Token node) {
    @NonNull final String text = node.getText();
    return text.substring(
      1,
      text.length() - 1
    ).replaceAll(
     "\\\\\\(",
     "("
    );
  }

  public @NonNull JPQLQuery transpile (@NonNull final CharSequence expression) {
    @NonNull final DateSelectionLexer lexer = new DateSelectionLexer(CharStreams.fromString(expression.toString()));

    @NonNull final DateSelectionParser parser = new DateSelectionParser(new CommonTokenStream(lexer));

    ParseTreeWalker.DEFAULT.walk(
      this,
      parser.selection()
    );

    return _currentSelection.build();
  }
}
