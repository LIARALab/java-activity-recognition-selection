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
import org.liara.selection.Utils;
import org.liara.selection.antlr.DateSelectionBaseListener;
import org.liara.selection.antlr.DateSelectionLexer;
import org.liara.selection.antlr.DateSelectionParser;
import org.liara.selection.antlr.DateSelectionParser.DefaultConfigurationContext;
import org.liara.selection.antlr.DateSelectionParser.FilterContext;
import org.liara.selection.antlr.DateSelectionParser.NegationContext;
import org.liara.selection.antlr.DateSelectionParser.SelectionContext;

public class DateTimeInRangeSelectionToExpressionCompiler
    extends DateSelectionBaseListener
    implements SelectionToExpressionCompiler<ZonedDateTime> {

  @NonNull
  private final ExpressionFactory _expressionFactory;
  @NonNull
  private final List<@NonNull Expression<@NonNull Boolean>> _stack;
  @NonNull
  private final DateSelectionLexer _lexer;
  @NonNull
  private final DateSelectionParser _parser;
  @NonNegative
  private int _offset;
  @NonNull
  private Expression<@NonNull ZonedDateTime> _lower;
  @NonNull
  private Expression<@NonNull ZonedDateTime> _upper;
  @NonNull
  private Locale _defaultLocale;
  @NonNull
  private DateTimeFormatter _defaultFormat;

  public DateTimeInRangeSelectionToExpressionCompiler() {
    _defaultLocale = Locale.getDefault();
    _defaultFormat = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    _stack = new ArrayList<>(20);
    _offset = 0;
    _expressionFactory = new ExpressionFactory();
    _lower = _expressionFactory.placeholder(Primitives.DATE_TIME);
    _upper = _expressionFactory.placeholder(Primitives.DATE_TIME);
    _lexer = new DateSelectionLexer(CharStreams.fromString(""));
    _parser = new DateSelectionParser(new CommonTokenStream(_lexer));
  }

  /**
   * @see DateTimeInRangeSelectionToExpressionCompiler#enterSelection(SelectionContext)
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
    @NonNull final List<@NonNull Expression<Boolean>> clauses = (
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

  @Override
  public void exitOperation(final DateSelectionParser.@NonNull OperationContext context) {
    @NonNull final PartialDate date = parseDate(context.date());
    @NonNegative final int operator = context.name == null ? DateSelectionParser.EQUAL
        : context.name.getType();

    switch (operator) {
      case DateSelectionParser.GREATHER_THAN_OR_EQUAL:
      case DateSelectionParser.GREATHER_THAN:
        _stack.add(compare(operator, _upper, date));
        break;
      case DateSelectionParser.LESS_THAN:
      case DateSelectionParser.LESS_THAN_OR_EQUAL:
        _stack.add(compare(operator, _lower, date));
        break;
      case DateSelectionParser.EQUAL:
      default:
        _stack.add(exitEquality(date));
        break;
    }
  }

  private @NonNull Expression<@NonNull Boolean> exitEquality(@NonNull final PartialDate date) {
    return _expressionFactory.and(
        compare(DateSelectionParser.LESS_THAN_OR_EQUAL, _lower, date),
        compare(DateSelectionParser.GREATHER_THAN_OR_EQUAL, _upper, date)
    );
  }

  private @NonNull Expression<@NonNull Boolean> compare(
      @NonNegative final int operator,
      @NonNull final Expression<@NonNull ZonedDateTime> left,
      @NonNull final PartialDate right
  ) {
    if (right.supportsDateTime()) {
      return compare(operator, zone(left, right.getZone()), toDateTime(right));
    } else {
      @NonNull final List<@NonNull Expression<@NonNull Boolean>> expressions = new ArrayList<>(3);

      compareAsDateIfPossible(operator, left, right).ifPresent(expressions::add);
      compareAsTimeIfPossible(operator, left, right).ifPresent(expressions::add);
      partiallyCompareIfPossible(operator, left, right).ifPresent(expressions::add);

      return _expressionFactory.and(expressions);
    }
  }


  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> partiallyCompareIfPossible(
      @NonNegative final int operator,
      @NonNull final Expression<@NonNull ZonedDateTime> left,
      @NonNull final PartialDate value
  ) {
    if (value.supportsPartials()) {
      switch (operator) {
        case DateSelectionParser.GREATHER_THAN:
        case DateSelectionParser.LESS_THAN:
          return partiallyStrictInequalTo(operator, left, value);
        case DateSelectionParser.GREATHER_THAN_OR_EQUAL:
        case DateSelectionParser.LESS_THAN_OR_EQUAL:
          return partiallyInequalTo(operator, left, value);
        default:
          return partiallyEqualTo(left, value);
      }
    } else {
      return Optional.empty();
    }
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> partiallyEqualTo(
      @NonNull final Expression<@NonNull ZonedDateTime> left,
      @NonNull final PartialDate value
  ) {
    @NonNull final Iterator<@NonNull ChronoField> fields = value.partialFields();
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> result = new LinkedList<>();

    while (fields.hasNext()) {
      result.add(compareField(DateSelectionParser.EQUAL, fields.next(), left, value));
    }

    return result.isEmpty() ? Optional.empty()
        : Optional.of(_expressionFactory.and(result));
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> partiallyInequalTo(
      @NonNegative final int operator,
      @NonNull final Expression<@NonNull ZonedDateTime> left,
      @NonNull final PartialDate value
  ) {
    @NonNull final Iterator<@NonNull ChronoField> fields = value.partialFields();
    @Nullable ChronoField previousField = null;
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> result = new LinkedList<>();

    while (fields.hasNext()) {
      @NonNull final ChronoField currentField = fields.next();

      if (previousField == null) {
        result.add(compareField(operator, currentField, left, value));
      } else {
        result.add(
            _expressionFactory.or(
                _expressionFactory.not(
                    compareField(DateSelectionParser.EQUAL, previousField, left, value)
                ),
                compareField(operator, currentField, left, value)
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
      @NonNull final Expression<@NonNull ZonedDateTime> left,
      @NonNull final PartialDate value
  ) {
    @NonNull final Iterator<@NonNull ChronoField> fields = value.partialFields();
    @Nullable ChronoField previousField = null;
    @NonNull final List<@NonNull Expression<@NonNull Boolean>> result = new LinkedList<>();

    while (fields.hasNext()) {
      @NonNull final ChronoField currentField = fields.next();

      if (previousField == null) {
        result.add(compareField(operator, currentField, left, value));
      } else {
        result.add(
            _expressionFactory.and(
                compareField(DateSelectionParser.EQUAL, previousField, left, value),
                compareField(operator, currentField, left, value)
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
      @NonNull final Expression<@NonNull ZonedDateTime> left,
      @NonNull final PartialDate right
  ) {
    if (right.supportsTime()) {
      return Optional.of(compare(operator, toTime(zone(left, right.getZone())), toTime(right)));
    } else {
      return Optional.empty();
    }
  }

  private @NonNull Optional<@NonNull Expression<@NonNull Boolean>> compareAsDateIfPossible(
      @NonNegative final int operator,
      @NonNull final Expression<@NonNull ZonedDateTime> left,
      @NonNull final PartialDate right
  ) {
    if (right.supportsDate()) {
      return Optional.of(compare(operator, toDate(zone(left, right.getZone())), toDate(right)));
    } else {
      return Optional.empty();
    }
  }


  /**
   * Return an expression that compare the given field of the value to compare to the given one.
   *
   * @param operator Kind of comparison to do.
   * @param field The datetime field to compare.
   * @param right The partial date to use as a comparison.
   * @return An expression that compare the given field of the value to compare to the given one.
   */
  private @NonNull Expression<@NonNull Boolean> compareField(
      @NonNegative final int operator,
      @NonNull final ChronoField field,
      @NonNull final Expression<@NonNull ZonedDateTime> left,
      @NonNull final PartialDate right
  ) {
    return compare(
        operator,
        JPQLDateTimeSelector.select(field, zone(left, right.getZone())),
        _expressionFactory.nonnull(right.getLong(field))
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
   * Return the given zoned date time in the given timezone.
   *
   * @param datetime A zoned date time to convert.
   * @param zone A target timezone.
   * @return The given zoned date time in the given timezone.
   */
  private @NonNull Expression<@NonNull ZonedDateTime> zone(
      @NonNull final Expression<@NonNull ZonedDateTime> datetime,
      @NonNull final ZoneId zone
  ) {
    return JPQLDateTimeSelector.zone(datetime, zone);
  }

  @Override
  public void exitRange(final DateSelectionParser.@NonNull RangeContext context) {
    @NonNull final DateTimeFormatter format = getFormatFrom(
        context.format(),
        getLocaleFrom(context.locale())
    );
    @NonNull final PartialDate left = PartialDate.from(
        format,
        getTokenContent(context.left)
    );
    @NonNull final PartialDate right = PartialDate.from(
        format,
        getTokenContent(context.right)
    );

    _stack.add(
        _expressionFactory.and(
            compare(DateSelectionParser.LESS_THAN_OR_EQUAL, _lower, Utils.max(left, right)),
            compare(DateSelectionParser.GREATHER_THAN_OR_EQUAL, _upper, Utils.min(left, right))
        )
    );
  }

  private @NonNull PartialDate parseDate(final DateSelectionParser.@NonNull DateContext date) {
    try {
      @NonNull final DateTimeFormatter format = getFormatFrom(
          date.format(),
          getLocaleFrom(date.locale())
      );

      @NonNull final String value = getTokenContent(date.value);

      return PartialDate.from(format, value);
    } catch (@NonNull final Throwable exception) {
      throw new Error(
          "Invalid date at line " + date.getStart().getLine() + " and index " +
              date.getStart().getCharPositionInLine() + " : \"" + date.getText()
              + "\"",
          exception
      );
    }
  }

  private @NonNull DateTimeFormatter getFormatFrom(
      final DateSelectionParser.@Nullable FormatContext format,
      @NonNull final Locale locale
  ) {
    return format == null ? _defaultFormat.withLocale(locale)
        : new DateTimeFormatterBuilder().parseStrict()
            .appendPattern(getTokenContent(format.TOKEN()))
            .toFormatter(locale)
        ;
  }

  private @NonNull Locale getLocaleFrom(final DateSelectionParser.@Nullable LocaleContext locale) {
    return (locale == null) ? _defaultLocale
        : Locale.forLanguageTag(getTokenContent(locale.TOKEN()));
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

  public @NonNull Expression<@NonNull ZonedDateTime> getLower() {
    return _lower;
  }

  public void setLower(@NonNull final Expression<@NonNull ZonedDateTime> lower) {
    _lower = lower;
  }

  public @NonNull Expression<@NonNull ZonedDateTime> getUpper() {
    return _upper;
  }

  public void setUpper(@NonNull final Expression<@NonNull ZonedDateTime> upper) {
    _upper = upper;
  }
}
