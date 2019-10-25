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
package org.liara.test.selection.datetime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.data.primitive.Primitives;
import org.liara.expression.Expression;
import org.liara.expression.ExpressionFactory;
import org.liara.selection.CompilationException;
import org.liara.selection.SelectionToExpressionCompiler;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.antlr.DateSelectionBaseListener;
import org.liara.selection.antlr.DateSelectionLexer;
import org.liara.selection.antlr.DateSelectionParser;
import org.liara.selection.antlr.DateSelectionParser.DefaultConfigurationContext;
import org.liara.selection.antlr.DateSelectionParser.FilterContext;
import org.liara.selection.antlr.DateSelectionParser.NegationContext;
import org.liara.selection.antlr.DateSelectionParser.OperationContext;
import org.liara.selection.antlr.DateSelectionParser.RangeContext;
import org.liara.selection.antlr.DateSelectionParser.SelectionContext;

public class DateTimeSelectionToExpressionCompiler
    extends DateSelectionBaseListener
    implements SelectionToExpressionCompiler<@NonNull ZonedDateTime> {

  @NonNull
  private final ExpressionFactory _expressionFactory;
  @NonNull
  private final List<@NonNull Expression<@NonNull Boolean>> _stack;
  @NonNull
  private final DateSelectionLexer _lexer;
  @NonNull
  private final DateSelectionParser _parser;
  @NonNull
  private Locale _defaultLocale;
  @NonNull
  private DateTimeFormatter _defaultFormat;
  @NonNegative
  private int _offset;
  @NonNull
  private Expression<@NonNull ZonedDateTime> _filteredValue;

  public DateTimeSelectionToExpressionCompiler() {
    _defaultLocale = Locale.getDefault();
    _defaultFormat = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    _expressionFactory = new ExpressionFactory();
    _filteredValue = _expressionFactory.placeholder(Primitives.DATE_TIME);
    _stack = new ArrayList<>(20);
    _lexer = new DateSelectionLexer(CharStreams.fromString(""));
    _parser = new DateSelectionParser(new CommonTokenStream(_lexer));
  }

  /**
   * @see DateSelectionBaseListener#enterSelection(SelectionContext)
   */
  @Override
  public void enterSelection(final DateSelectionParser.@NonNull SelectionContext context) {
    _defaultLocale = Locale.getDefault();
    _defaultFormat = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    _stack.clear();
    _offset = 0;
  }

  /**
   * @see DateSelectionBaseListener#exitSelection(SelectionContext)
   */
  @Override
  public void exitSelection(final DateSelectionParser.@NonNull SelectionContext context) {
    @NonNull final Expression<@NonNull Boolean> selection = _expressionFactory.or(_stack);
    _stack.clear();
    _stack.add(selection);
  }

  /**
   * @see DateSelectionBaseListener#exitFilter(FilterContext)
   */
  @Override
  public void exitFilter(final DateSelectionParser.@NonNull FilterContext context) {
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> clauses = (
        _stack.subList(_offset, _stack.size())
    );

    @NonNull final Expression<@NonNull Boolean> filter = _expressionFactory.and(clauses);

    clauses.clear();
    _stack.add(filter);
    _offset += 1;
  }

  /**
   * @see DateSelectionBaseListener#exitDefaultConfiguration(DefaultConfigurationContext)
   */
  @Override
  public void exitDefaultConfiguration(
      final DateSelectionParser.@NonNull DefaultConfigurationContext context
  ) {
    _defaultLocale = getLocaleFrom(context.locale());
    _defaultFormat = getFormatFrom(context.format(), _defaultLocale);
  }

  /**
   * @see DateSelectionBaseListener#exitNegation(NegationContext)
   */
  @Override
  public void exitNegation(final DateSelectionParser.@NonNull NegationContext context) {
    _stack.set(_stack.size() - 1, _expressionFactory.not(_stack.get(_stack.size() - 1)));
  }

  /**
   * @see DateSelectionBaseListener#exitOperation(OperationContext)
   */
  @Override
  public void exitOperation(final DateSelectionParser.@NonNull OperationContext context) {
    _stack.add(
        compare(
            context.name == null ? DateSelectionParser.EQUAL : context.name.getType(),
            parseDate(context.date())
        )
    );
  }

  private @NonNull Expression<@NonNull Boolean> compare(
      @NonNegative final int operator,
      @NonNull final PartialDate value
  ) {
    if (value.supportsDateTime()) {
      return compare(operator, getZonedValue(value.getZone()), toDateTime(value));
    } else {
      @NonNull final List<@NonNull Expression<@NonNull Boolean>> expressions = new ArrayList<>(3);

      compareAsDateIfPossible(operator, value).ifPresent(expressions::add);
      compareAsTimeIfPossible(operator, value).ifPresent(expressions::add);
      partiallyCompareIfPossible(operator, value).ifPresent(expressions::add);

      return _expressionFactory.and(expressions);
    }
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> partiallyCompareIfPossible(
      @NonNegative final int operator,
      @NonNull final PartialDate value
  ) {
    if (value.supportsPartials()) {
      switch (operator) {
        case DateSelectionParser.GREATHER_THAN:
        case DateSelectionParser.LESS_THAN:
          return partiallyStrictInequalTo(operator, value);
        case DateSelectionParser.GREATHER_THAN_OR_EQUAL:
        case DateSelectionParser.LESS_THAN_OR_EQUAL:
          return partiallyInequalTo(operator, value);
        default:
          return partiallyEqualTo(value);
      }
    } else {
      return Optional.empty();
    }
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> partiallyEqualTo(
      @NonNull final PartialDate value
  ) {
    @NonNull final Iterator<@NonNull ChronoField> fields = value.partialFields();
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> result = new LinkedList<>();

    while (fields.hasNext()) {
      result.add(compareField(DateSelectionParser.EQUAL, fields.next(), value));
    }

    return result.isEmpty() ? Optional.empty()
        : Optional.of(_expressionFactory.and(result));
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> partiallyInequalTo(
      @NonNegative final int operator,
      @NonNull final PartialDate value
  ) {
    @NonNull final Iterator<@NonNull ChronoField> fields = value.partialFields();
    @Nullable ChronoField previousField = null;
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> result = new LinkedList<>();

    while (fields.hasNext()) {
      @NonNull final ChronoField currentField = fields.next();

      if (previousField == null) {
        result.add(compareField(operator, currentField, value));
      } else {
        result.add(
            _expressionFactory.or(
                _expressionFactory.not(
                    compareField(DateSelectionParser.EQUAL, previousField, value)
                ),
                compareField(operator, currentField, value)
            )
        );
      }

      previousField = currentField;
    }

    return result.isEmpty() ? Optional.empty()
        : Optional.of(_expressionFactory.and(result));
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> partiallyStrictInequalTo(
      @NonNegative final int operator,
      @NonNull final PartialDate value
  ) {
    @NonNull final Iterator<@NonNull ChronoField> fields = value.partialFields();
    @Nullable ChronoField previousField = null;
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> result = new LinkedList<>();

    while (fields.hasNext()) {
      @NonNull final ChronoField currentField = fields.next();

      if (previousField == null) {
        result.add(compareField(operator, currentField, value));
      } else {
        result.add(
            _expressionFactory.and(
                compareField(DateSelectionParser.EQUAL, previousField, value),
                compareField(operator, currentField, value)
            )
        );
      }

      previousField = currentField;
    }

    return result.isEmpty() ? Optional.empty()
        : Optional.of(_expressionFactory.or(result));
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> compareAsTimeIfPossible(
      @NonNegative final int operator,
      @NonNull final PartialDate value
  ) {
    if (value.supportsTime()) {
      return Optional.of(compare(operator, toTime(getZonedValue(value.getZone())), toTime(value)));
    } else {
      return Optional.empty();
    }
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> compareAsDateIfPossible(
      @NonNegative final int operator,
      @NonNull final PartialDate value
  ) {
    if (value.supportsDate()) {
      return Optional.of(compare(operator, toDate(getZonedValue(value.getZone())), toDate(value)));
    } else {
      return Optional.empty();
    }
  }

  /**
   * Return an expression that compare the given field of the value to compare to the given one.
   *
   * @param operator Kind of comparison to do.
   * @param field The datetime field to compare.
   * @param value The partial date to use as a comparison.
   * @return An expression that compare the given field of the value to compare to the given one.
   */
  private @NonNull Expression<@NonNull Boolean> compareField(
      @NonNegative final int operator,
      @NonNull final ChronoField field,
      @NonNull final PartialDate value
  ) {
    return compare(
        operator,
        JPQLDateTimeSelector.select(field, getZonedValue(value.getZone())),
        _expressionFactory.nonnull(value.getLong(field))
    );
  }

  /**
   * Return an operation that compares two values.
   *
   * @param operator Kind of comparision to do.
   * @param left Left value.
   * @param right Right value.
   * @param <Value> Type of value to compare.
   * @return An operation that compares two values.
   */
  private <Value extends Comparable<? super Value>> @NonNull Expression<@NonNull Boolean> compare(
      @NonNegative final int operator,
      @NonNull final Expression<@NonNull Value> left,
      @NonNull final Expression<@NonNull Value> right
  ) {
    switch (operator) {
      case DateSelectionParser.GREATHER_THAN:
        return _expressionFactory.greaterThan(left, right);
      case DateSelectionParser.GREATHER_THAN_OR_EQUAL:
        return _expressionFactory.greaterThanOrEqual(left, right);
      case DateSelectionParser.LESS_THAN:
        return _expressionFactory.lessThan(left, right);
      case DateSelectionParser.LESS_THAN_OR_EQUAL:
        return _expressionFactory.lessThanOrEqual(left, right);
      default:
        return _expressionFactory.equal(left, right);
    }
  }

  /**
   * Transform a partial date into a zoned date time.
   *
   * @param date A partial date to transform.
   * @return A zoned date time built from the given partial date.
   */
  private @NonNull Expression<@NonNull ZonedDateTime> toDateTime(@NonNull final PartialDate date) {
    return _expressionFactory.nonnull(LocalDateTime.from(date).atZone(ZoneId.of("UTC")));
  }

  /**
   * Transform a partial date into a date.
   *
   * @param date A partial date to transform.
   * @return A date built from the given partial date.
   */
  private @NonNull Expression<@NonNull LocalDate> toDate(@NonNull final PartialDate date) {
    return _expressionFactory.nonnull(LocalDate.from(date));
  }

  /**
   * Transform a ZonedDateTime expression into a date expression.
   *
   * @param date A ZonedDateTime expression to transform.
   * @return A date built from the given ZonedDateTime expression.
   */
  private @NonNull Expression<@NonNull LocalDate> toDate(
      @NonNull final Expression<@NonNull ZonedDateTime> date
  ) {
    return JPQLDateTimeSelector.toDate(date);
  }

  /**
   * Transform a ZonedDateTime expression into a time expression.
   *
   * @param date A ZonedDateTime expression to transform.
   * @return A time built from the given ZonedDateTime expression.
   */
  private @NonNull Expression<@NonNull LocalTime> toTime(
      @NonNull final Expression<@NonNull ZonedDateTime> date
  ) {
    return JPQLDateTimeSelector.toTime(date);
  }

  /**
   * Transform a partial date into a time.
   *
   * @param date A partial date to transform.
   * @return A time built from the given partial date.
   */
  private @NonNull Expression<@NonNull LocalTime> toTime(@NonNull final PartialDate date) {
    return _expressionFactory.nonnull(LocalTime.from(date));
  }

  /**
   * Return the value to compare in the given timezone.
   *
   * @param zone A target timezone.
   * @return The value to compare in the given timezone.
   */
  private @NonNull Expression<@NonNull ZonedDateTime> getZonedValue(@NonNull final ZoneId zone) {
    return JPQLDateTimeSelector.zone(_filteredValue, zone);
  }

  /**
   * @see DateSelectionBaseListener#exitRange(RangeContext)
   */
  @Override
  public void exitRange(final DateSelectionParser.@NonNull RangeContext context) {
    @NonNull final DateTimeFormatter format = (
        getFormatFrom(context.format(), getLocaleFrom(context.locale()))
    );

    @NonNull final PartialDate left = PartialDate.from(format, getTokenContent(context.left));
    @NonNull final PartialDate right = PartialDate.from(format, getTokenContent(context.right));

    _stack.add(
        _expressionFactory.and(
            compare(DateSelectionParser.GREATHER_THAN_OR_EQUAL, left),
            compare(DateSelectionParser.LESS_THAN_OR_EQUAL, right)
        )
    );
  }

  /**
   * Extract a partial date from a given date context.
   *
   * @param date A context to evaluate.
   * @return A partial date extracted from the given context.
   */
  private @NonNull PartialDate parseDate(final DateSelectionParser.@NonNull DateContext date) {
    try {
      return PartialDate.from(
          getFormatFrom(date.format(), getLocaleFrom(date.locale())),
          getTokenContent(date.value)
      );
    } catch (@NonNull final Throwable exception) {
      @NonNull final DateTimeFormatter format = getFormatFrom(
          date.format(),
          getLocaleFrom(date.locale())
      );

      throw new Error(
          "Invalid date at line " + date.getStart().getLine() + " and index " +
              date.getStart().getCharPositionInLine() + " : \"" + getTokenContent(date.value)
              + "\". A date of " + format.toString() +
              " format was expected, and the parser raised : " + exception.getMessage(),
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
   * @return The filtered value.
   */
  public @NonNull Expression<@NonNull ZonedDateTime> getFilteredValue() {
    return _filteredValue;
  }

  /**
   * Change the expression of the value to filter.
   *
   * @param value The expression of the new value to filter.
   */
  public void setFilteredValue(@NonNull final Expression<@NonNull ZonedDateTime> value) {
    _filteredValue = value;
  }
}
