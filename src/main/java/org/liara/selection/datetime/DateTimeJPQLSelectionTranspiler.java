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
package org.liara.selection.datetime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Iterator;
import java.util.Locale;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.TranspilationException;
import org.liara.selection.Utils;
import org.liara.selection.antlr.DateSelectionBaseListener;
import org.liara.selection.antlr.DateSelectionLexer;
import org.liara.selection.antlr.DateSelectionParser;
import org.liara.selection.jpql.JPQLClauseBuilder;
import org.liara.selection.jpql.JPQLQuery;
import org.liara.selection.jpql.JPQLQueryBuilder;
import org.liara.selection.jpql.JPQLSelectionTranspiler;

public class DateTimeJPQLSelectionTranspiler
    extends DateSelectionBaseListener
    implements JPQLSelectionTranspiler {

  @NonNull
  private final JPQLClauseBuilder _currentClause;

  @NonNull
  private final JPQLQueryBuilder _currentSelection;

  @NonNull
  private Locale _defaultLocale;

  @NonNull
  private DateTimeFormatter _defaultFormat;

  public DateTimeJPQLSelectionTranspiler() {
    _currentClause = new JPQLClauseBuilder();
    _currentSelection = new JPQLQueryBuilder();
    _defaultLocale = Locale.getDefault();
    _defaultFormat = DateTimeFormatter.ISO_ZONED_DATE_TIME;
  }

  @Override
  public void enterSelection(
      final DateSelectionParser.@NonNull SelectionContext context
  ) {
    _defaultLocale = Locale.getDefault();
    _defaultFormat = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    _currentSelection.reset();
  }

  @Override
  public void exitFilter(final DateSelectionParser.@NonNull FilterContext context) {
    _currentSelection.appendFilter();
  }

  @Override
  public void exitDefaultConfiguration(
      final DateSelectionParser.@NonNull DefaultConfigurationContext context
  ) {
    _defaultLocale = getLocaleFrom(context.locale());
    _defaultFormat = getFormatFrom(context.format(), _defaultLocale);
  }

  @Override
  public void enterClause(
      final DateSelectionParser.@NonNull ClauseContext context
  ) {
    _currentClause.reset();
  }

  @Override
  public void exitClause(
      final DateSelectionParser.@NonNull ClauseContext context
  ) {
    _currentSelection.appendClause(_currentClause);
  }

  @Override
  public void exitNegation(
      final DateSelectionParser.@NonNull NegationContext context
  ) {
    _currentClause.negate();
  }

  @Override
  public void exitOperation(final DateSelectionParser.@NonNull OperationContext context) {
    appendOperation(getOperator(context.name), parseDate(context.date()), "value");
  }

  private void appendOperation(
      @NonNull final String operator,
      @NonNull final PartialDate date,
      @NonNull final String parameterName
  ) {
    boolean first = true;

    if (date.supportsDate() || date.supportsTime()) {
      _currentClause.appendLiteral(getSelfLiteralForPlainComparisonWith(date));
      _currentClause.appendLiteral(operator);
      _currentClause.appendParameter(parameterName, toValueForPlainComparison(date));

      first = false;
    }

    if (date.supportsPartials()) {
      @NonNull final Iterator<@NonNull ChronoField> fields = date.partialFields();
      int parenthesisToClose = 0;
      @Nullable ChronoField previousField = null;

      if (!first) {
        _currentClause.appendLiteral("AND");
      }

      while (fields.hasNext()) {
        @NonNull final ChronoField field = fields.next();

        if (previousField != null) {
          if (">".equals(operator) || "<".equals(operator)) {
            _currentClause.appendLiteral("OR");
            _currentClause.append(" (");
            _currentClause.append(getSelfSelectionForComparisonWith(date, previousField));
            _currentClause.appendLiteral("=");
            _currentClause.appendParameter(
                getParameterName(parameterName, previousField),
                date.get(previousField)
            );
            _currentClause.appendLiteral("AND");
            parenthesisToClose += 1;
          } else if (">=".equals(operator) || "<=".equals(operator)) {
            _currentClause.appendLiteral("AND");
            _currentClause.append(" (");
            _currentClause.append(getSelfSelectionForComparisonWith(date, previousField));
            _currentClause.appendLiteral("!=");
            _currentClause.appendParameter(
                getParameterName(parameterName, previousField),
                date.get(previousField)
            );
            _currentClause.appendLiteral("OR");
            parenthesisToClose += 1;
          } else {
            _currentClause.appendLiteral("AND");
          }
        }

        _currentClause.appendLiteral(getSelfSelectionForComparisonWith(date, field));
        _currentClause.appendLiteral(operator);
        _currentClause.appendParameter(getParameterName(parameterName, field), date.get(field));

        previousField = field;
      }

      while (parenthesisToClose-- > 0) {
        _currentClause.append(")");
      }
    }
  }

  private @NonNull String getParameterName(
      @NonNull final String prefix,
      @NonNull final ChronoField field
  ) {
    return prefix + "_" + field.toString().toLowerCase();
  }

  private @NonNull String getSelfSelectionForComparisonWith(
      @NonNull final PartialDate date,
      @NonNull final ChronoField field
  ) {
    return JPQLDateTimeSelector.select(
        field,
        JPQLDateTimeSelector.zone(_currentClause.self(), date.getZone())
    );
  }

  private @NonNull Object toValueForPlainComparison(@NonNull final PartialDate date) {
    if (date.supportsDateTime()) {
      return LocalDateTime.from(date).atZone(ZoneId.of("UTC"));
    } else if (date.supportsDate()) {
      return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
    } else {
      return DateTimeFormatter.ISO_LOCAL_TIME.format(date);
    }
  }

  private @NonNull String getSelfLiteralForPlainComparisonWith(@NonNull final PartialDate date) {
    @NonNull final String zonedSelf = JPQLDateTimeSelector.zone(
        _currentClause.self(),
        date.getZone()
    );

    if (date.supportsDateTime()) {
      return zonedSelf;
    } else if (date.supportsDate()) {
      return JPQLDateTimeSelector.toDate(zonedSelf);
    } else {
      return JPQLDateTimeSelector.toTime(zonedSelf);
    }
  }

  private @NonNull String getOperator(@Nullable final Token operator) {
    final int type = operator == null ? DateSelectionLexer.EQUAL : operator.getType();

    switch (type) {
      case DateSelectionLexer.GREATHER_THAN:
        return ">";
      case DateSelectionLexer.GREATHER_THAN_OR_EQUAL:
        return ">=";
      case DateSelectionLexer.LESS_THAN:
        return "<";
      case DateSelectionLexer.LESS_THAN_OR_EQUAL:
        return "<=";
      default:
        return "=";
    }
  }

  @Override
  public void exitRange(final DateSelectionParser.@NonNull RangeContext context) {
    @NonNull final DateTimeFormatter format = (
        getFormatFrom(context.format(), getLocaleFrom(context.locale()))
    );

    @NonNull final PartialDate left = (
        PartialDate.from(format, getTokenContent(context.left))
    );

    @NonNull final PartialDate right = (
        PartialDate.from(format, getTokenContent(context.right))
    );

    appendBetween(Utils.min(left, right), Utils.max(left, right));
  }

  private void appendBetween(@NonNull final PartialDate min, @NonNull final PartialDate max) {
    appendOperation(">=", min, "min");
    _currentClause.appendLiteral("AND");
    appendOperation("<=", max, "max");
  }

  private @NonNull PartialDate parseDate(final DateSelectionParser.@NonNull DateContext date) {
    try {
      @NonNull final DateTimeFormatter format = (
          getFormatFrom(date.format(), getLocaleFrom(date.locale()))
      );

      @NonNull final String value = getTokenContent(date.value);

      return PartialDate.from(format, value);
    } catch (@NonNull final Throwable exception) {
      throw new Error(
          "Invalid date at line " + String.valueOf(date.getStart().getLine()) + " and index " +
              String.valueOf(date.getStart().getCharPositionInLine()) + " : \"" + date.getText()
              + "\"",
          exception
      );
    }
  }

  private @NonNull DateTimeFormatter getFormatFrom(
      final DateSelectionParser.@Nullable FormatContext format,
      @NonNull final Locale locale
  ) {
    return format == null ? _defaultFormat.withLocale(locale) : (
        new DateTimeFormatterBuilder().parseStrict()
            .appendPattern(getTokenContent(format.TOKEN()))
            .toFormatter(locale)
    );
  }

  private @NonNull Locale getLocaleFrom(final DateSelectionParser.@Nullable LocaleContext locale) {
    return locale == null ? _defaultLocale : Locale.forLanguageTag(getTokenContent(locale.TOKEN()));
  }

  private @NonNull String getTokenContent(@NonNull final TerminalNode node) {
    @NonNull final String text = node.getText();
    return text.substring(1, text.length() - 1).replaceAll("\\\\\\(", "(");
  }

  private @NonNull String getTokenContent(@NonNull final Token node) {
    @NonNull final String text = node.getText();
    return text.substring(1, text.length() - 1).replaceAll("\\\\\\(", "(");
  }

  public @NonNull JPQLQuery transpile(@NonNull final CharSequence expression) {
    @NonNull final DateSelectionLexer lexer = (
        new DateSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    @NonNull final DateSelectionParser parser = (
        new DateSelectionParser(new CommonTokenStream(lexer))
    );

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _currentSelection.build();
  }

  public @NonNull JPQLQuery tryToTranspile(@NonNull final CharSequence expression)
      throws TranspilationException {
    @NonNull final DateSelectionLexer lexer = (
        new DateSelectionLexer(CharStreams.fromString(expression.toString()))
    );

    @NonNull final DateSelectionParser parser = (
        new DateSelectionParser(new CommonTokenStream(lexer))
    );

    lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
    parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, parser.selection());

    return _currentSelection.build();
  }
}
