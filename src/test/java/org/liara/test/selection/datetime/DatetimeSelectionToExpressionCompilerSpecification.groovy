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

package org.liara.test.selection.datetime

import org.liara.expression.ExpressionFactory
import spock.lang.Specification

import java.time.*

class DatetimeSelectionToExpressionCompilerSpecification
        extends Specification {

    def setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    def "#compile can compile greater than clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a greater than clause"
        final String value = '2018-12-10T15:20:30'

        compiler.compile("gt:(${value}Z[Europe/Paris])") == factory.greaterThan(
                JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                factory.nonnull(LocalDateTime.parse(value).atZone(ZoneId.of("UTC")))
        )
    }

    def "#compile can compile greater than or equal clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a greater than or equal clause"
        final String value = "2018-12-10T15:20:30"

        compiler.compile("gte:(${value}Z[Europe/Paris])") == factory.greaterThanOrEqual(
                JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                factory.nonnull(LocalDateTime.parse(value).atZone(ZoneId.of("UTC")))
        )
    }

    def "#compile can compile less than clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a less than clause"
        final String value = "2018-12-10T15:20:30"

        compiler.compile("lt:(${value}Z[Europe/Paris])") == factory.lessThan(
                JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                factory.nonnull(LocalDateTime.parse(value).atZone(ZoneId.of("UTC")))
        )
    }

    def "#compile can compile less than or equal clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a less than or equal clause"
        final String value = "2018-12-10T15:20:30"

        compiler.compile("lte:(${value}Z[Europe/Paris])") == factory.lessThanOrEqual(
                JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                factory.nonnull(LocalDateTime.parse(value).atZone(ZoneId.of("UTC")))
        )
    }

    def "#compile can compile equal clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile equal clauses"
        final String value = '2018-12-10T15:20:30'

        compiler.compile("(${value}Z[Europe/Paris])") == factory.equal(
                JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                factory.nonnull(LocalDateTime.parse(value).atZone(ZoneId.of("UTC")))
        )

        compiler.compile("eq:(${value}Z[Europe/Paris])") == factory.equal(
                JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                factory.nonnull(LocalDateTime.parse(value).atZone(ZoneId.of("UTC")))
        )
    }

    def "#compile can compile range clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a range clause"
        final String left = "2018-12-16T18:10:20"
        final String right = "2018-12-16T16:20:30"

        compiler.compile("(${left}Z[America/New_York]):(${right}Z[Europe/Paris])") == factory.and(
                factory.greaterThanOrEqual(
                        JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("America/New_York")),
                        factory.nonnull(LocalDateTime.parse(left).atZone(ZoneId.of("UTC")))
                ),
                factory.lessThanOrEqual(
                        JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                        factory.nonnull(LocalDateTime.parse(right).atZone(ZoneId.of("UTC")))
                )

        )

        compiler.compile("(${right}Z[Europe/Paris]):(${left}Z[America/New_York])") == factory.and(
                factory.greaterThanOrEqual(
                        JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                        factory.nonnull(LocalDateTime.parse(right).atZone(ZoneId.of("UTC")))
                ),
                factory.lessThanOrEqual(
                        JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("America/New_York")),
                        factory.nonnull(LocalDateTime.parse(left).atZone(ZoneId.of("UTC")))
                )

        )
    }

    def "#compile can compile negation of clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile negations"
        final String filter = 'eq:(2018-12-10T15:20:30Z[Europe/Paris])'
        compiler.compile("not:$filter") == factory.not(compiler.compile(filter))
    }

    def "#compile can compile conjunction of clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a conjunction of clauses"
        final String first = "not:eq:(2018-12-10T15:20:30Z[Europe/Paris])"
        final String second = "lt:(2018-12-20T15:20:30Z[Europe/Paris])"
        final String third = "gt:(2018-12-03T15:20:30Z[Europe/Paris])"

        compiler.compile("$first,$second,$third") == factory.and(Arrays.asList(
                compiler.compile(first),
                compiler.compile(second),
                compiler.compile(third)
        ))
    }

    def "#compile can compile disjunction of clauses"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile disjunction of clauses"
        final String left = "not:eq:(2018-12-10T15:20:30Z[Europe/Paris])," +
                "lt:(2018-12-20T15:20:30Z[Europe/Paris])"
        final String right = "gt:(2018-12-03T15:20:30Z[Europe/Paris])"

        compiler.compile("$left;$right") == factory.or(
                compiler.compile(left),
                compiler.compile(right)
        )
    }

    def "#compile can compile datetime"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a datetime"
        compiler.compile("(2018-12-10T15:20:30+01:00[Europe/Paris])") == factory.equal(
                JPQLDateTimeSelector.zone(compiler.getFilteredValue(), ZoneId.of("Europe/Paris")),
                factory.nonnull(LocalDateTime.parse('2018-12-10T15:20:30').atZone(ZoneId.of("UTC")))
        )

        compiler.compile("(2018-12-10T15:20:30+00:00[UTC])") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(LocalDateTime.parse('2018-12-10T15:20:30').atZone(ZoneId.of("UTC")))
        )
    }

    def "#compile can compile date"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a date"
        compiler.compile("format:(yyyy-MM-dd)(2018-12-10)") == factory.equal(
                JPQLDateTimeSelector.toDate(compiler.getFilteredValue()),
                factory.nonnull(LocalDate.parse("2018-12-10"))
        )

        compiler.compile("format:(yyyy-MM-ddX)(2018-12-10+05)") == factory.equal(
                JPQLDateTimeSelector.toDate(JPQLDateTimeSelector.zone(
                        compiler.getFilteredValue(),
                        ZoneId.ofOffset("", ZoneOffset.ofHours(5))
                )),
                factory.nonnull(LocalDate.parse("2018-12-10"))
        )
    }

    def "#compile can compile time"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a time"
        compiler.compile("format:(HH:mm:ss)(15:20:30)") == factory.equal(
                JPQLDateTimeSelector.toTime(compiler.getFilteredValue()),
                factory.nonnull(LocalTime.parse("15:20:30"))
        )

        compiler.compile("format:(HH:mm:ssX)(15:20:30+05)") == factory.equal(
                JPQLDateTimeSelector.toTime(JPQLDateTimeSelector.zone(
                        compiler.getFilteredValue(),
                        ZoneId.ofOffset("", ZoneOffset.ofHours(5))
                )),
                factory.nonnull(LocalTime.parse("15:20:30"))
        )
    }

    def "#compile can compile fully custom format"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a fully custom format"
        compiler.compile("locale:(en)format:(EEEE HH'h')(Monday 15h)") == factory.and(
                factory.equal(
                        JPQLDateTimeSelector.selectDayOfWeek(compiler.getFilteredValue()),
                        factory.nonnull(1L)
                ),
                factory.equal(
                        JPQLDateTimeSelector.selectHourOfDay(compiler.getFilteredValue()),
                        factory.nonnull(15L)
                )
        )

        compiler.compile("locale:(en)format:(EEEE HH'h'X)(Monday 15h+05)") == factory.and(
                factory.equal(
                        JPQLDateTimeSelector.selectDayOfWeek(JPQLDateTimeSelector.zone(
                                compiler.getFilteredValue(),
                                ZoneId.ofOffset("", ZoneOffset.ofHours(5))
                        )),
                        factory.nonnull(1L)
                ),
                factory.equal(
                        JPQLDateTimeSelector.selectHourOfDay(JPQLDateTimeSelector.zone(
                                compiler.getFilteredValue(),
                                ZoneId.ofOffset("", ZoneOffset.ofHours(5))
                        )),
                        factory.nonnull(15L)
                )
        )
    }

    def "#compile can compile equality of partial dates"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a fully custom format"
        compiler.compile("eq:locale:(en)format:(EEEE HH'h'mm'm')(Tuesday 20h30m)") == factory.and(Arrays.asList(
                factory.equal(
                        JPQLDateTimeSelector.selectDayOfWeek(compiler.getFilteredValue()),
                        factory.nonnull(2L)
                ),
                factory.equal(
                        JPQLDateTimeSelector.selectHourOfDay(compiler.getFilteredValue()),
                        factory.nonnull(20L)
                ),
                factory.equal(
                        JPQLDateTimeSelector.selectMinuteOfHour(compiler.getFilteredValue()),
                        factory.nonnull(30L)
                )
        ))
    }

    def "#compile can compile strict inequality of partial dates"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile strict inequality of partial dates"
        compiler.compile("lt:locale:(en)format:(EEEE HH'h'mm'm')(Tuesday 20h30m)") == factory.or(Arrays.asList(
                factory.lessThan(
                        JPQLDateTimeSelector.selectDayOfWeek(compiler.getFilteredValue()),
                        factory.nonnull(2L)
                ),
                factory.and(
                        factory.equal(
                                JPQLDateTimeSelector.selectDayOfWeek(compiler.getFilteredValue()),
                                factory.nonnull(2L)
                        ),
                        factory.lessThan(
                                JPQLDateTimeSelector.selectHourOfDay(compiler.getFilteredValue()),
                                factory.nonnull(20L)
                        )
                ),
                factory.and(
                        factory.equal(
                                JPQLDateTimeSelector.selectHourOfDay(compiler.getFilteredValue()),
                                factory.nonnull(20L)
                        ),
                        factory.lessThan(
                                JPQLDateTimeSelector.selectMinuteOfHour(compiler.getFilteredValue()),
                                factory.nonnull(30L)
                        )
                )
        ))
    }

    def "#compile can compile loose inequality of partial dates"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a fully custom format"
        compiler.compile("lte:locale:(en)format:(EEEE HH'h'mm'm')(Tuesday 20h30m)") == factory.and(Arrays.asList(
                factory.lessThanOrEqual(
                        JPQLDateTimeSelector.selectDayOfWeek(compiler.getFilteredValue()),
                        factory.nonnull(2L)
                ),
                factory.or(
                        factory.not(factory.equal(
                                JPQLDateTimeSelector.selectDayOfWeek(compiler.getFilteredValue()),
                                factory.nonnull(2L)
                        )),
                        factory.lessThanOrEqual(
                                JPQLDateTimeSelector.selectHourOfDay(compiler.getFilteredValue()),
                                factory.nonnull(20L)
                        )
                ),
                factory.or(
                        factory.not(factory.equal(
                                JPQLDateTimeSelector.selectHourOfDay(compiler.getFilteredValue()),
                                factory.nonnull(20L)
                        )),
                        factory.lessThanOrEqual(
                                JPQLDateTimeSelector.selectMinuteOfHour(compiler.getFilteredValue()),
                                factory.nonnull(30L)
                        )
                )
        ))
    }

    def "#compile can compile partial ranges"() {
        given: "a compiler"
        final DateTimeSelectionToExpressionCompiler compiler = new DateTimeSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a fully custom format"
        compiler.compile("locale:(en)format:(EEEE HH'h'mm'm')(Tuesday 20h30m):(Monday 15h20m)") == factory.and(
                compiler.compile("gte:locale:(en)format:(EEEE HH'h'mm'm')(Tuesday 20h30m)"),
                compiler.compile("lte:locale:(en)format:(EEEE HH'h'mm'm')(Monday 15h20m)")
        )
    }
}
