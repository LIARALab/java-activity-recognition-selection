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

class DatetimeInRangeJPQLSelectionTranspilerSpecification
        extends Specification {
    def setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    def "it can compile greater than clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        expect: "to be able to compile a greater than clause"
        final String date = '2018-12-10T15:20:30Z[Europe/Paris]'
        compiler.compile("gt:($date)") == upper.compile("gt:($date)")
    }

    def "it can compile greater than or equal clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        expect: "to be able to compile a greater than or equal clause"
        final String date = '2018-12-10T15:20:30Z[Europe/Paris]'
        compiler.compile("gte:($date)") == upper.compile("gte:($date)")
    }

    def "it can compile less than clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        expect: "to be able to compile a less than clause"
        final String date = '2018-12-10T15:20:30Z[Europe/Paris]'
        compiler.compile("lt:($date)") == lower.compile("lt:($date)")
    }

    def "it can compile less than or equal clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        expect: "to be able to compile a less than or equal clause"
        final String date = '2018-12-10T15:20:30Z[Europe/Paris]'
        compiler.compile("lte:($date)") == lower.compile("lte:($date)")
    }

    def "it can compile equal clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        expect: "to be able to compile an equal clause"
        final String date = '2018-12-10T15:20:30Z[Europe/Paris]'
        compiler.compile("eq:($date)") == factory.and(
                lower.compile("lte:($date)"),
                upper.compile("gte:($date)")
        )
    }

    def "it can compile range clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        expect: "to be able to compile a range clause"
        final String left = '2018-12-10T15:20:30Z[Europe/Paris]'
        final String right = '2018-12-16T18:25:30Z[Europe/Paris]'

        compiler.compile("($left):($right)") == factory.and(
                lower.compile("lte:($right)"),
                upper.compile("gte:($left)")
        )
    }

    def "it can compile negation of clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a negation"
        final String filter = '(2018-12-10T15:20:30Z[Europe/Paris]):(2018-12-16T18:25:30Z[Europe/Paris])'

        compiler.compile("not:$filter") == factory.not(compiler.compile(filter))
    }

    def "it can compile conjunction of clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a conjunction"
        final String first = 'not:eq:(2018-12-10T15:20:30Z[Europe/Paris])'
        final String second = 'lt:(2018-12-20T15:20:30Z[Europe/Paris])'
        final String third = 'gt:(2018-12-03T15:20:30Z[Europe/Paris])'

        compiler.compile("$first,$second,$third") == factory.and(Arrays.asList(
                compiler.compile(first),
                compiler.compile(second),
                compiler.compile(third)
        ))
    }

    def "it can compile disjunction of clauses"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a disjunction"
        final String first = 'not:eq:(2018-12-10T15:20:30Z[Europe/Paris])'
        final String second = 'lt:(2018-12-20T15:20:30Z[Europe/Paris])'
        final String third = 'gt:(2018-12-03T15:20:30Z[Europe/Paris])'

        compiler.compile("$first,$second;$third") == factory.or(Arrays.asList(
                compiler.compile("$first,$second"),
                compiler.compile(third)
        ))
    }

    def "it can compile datetime"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a datetime"
        final String date = '2018-12-10T15:20:30Z[Europe/Paris]'
        compiler.compile("($date)") == factory.and(
                lower.compile("lte:($date)"),
                upper.compile("gte:($date)")
        )
    }

    def "it can compile date"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a date"
        final String date = '2018-12-10'
        compiler.compile("format:(yyyy-MM-dd)($date)") == factory.and(
                lower.compile("lte:format:(yyyy-MM-dd)($date)"),
                upper.compile("gte:format:(yyyy-MM-dd)($date)")
        )
    }

    def "it can compile time"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a time"
        final String date = '15:20:30'
        compiler.compile("format:(HH:mm:ss)($date)") == factory.and(
                lower.compile("lte:format:(HH:mm:ss)($date)"),
                upper.compile("gte:format:(HH:mm:ss)($date)")
        )
    }

    def "it can compile fully custom format"() {
        given: "a compiler"
        final DateTimeInRangeSelectionToExpressionCompiler compiler = (
                new DateTimeInRangeSelectionToExpressionCompiler()
        )

        and: "a lower bound compiler"
        final DateTimeSelectionToExpressionCompiler lower = (
                new DateTimeSelectionToExpressionCompiler()
        )
        lower.setFilteredValue(compiler.getLower())

        and: "an upper bound compiler"
        final DateTimeSelectionToExpressionCompiler upper = (
                new DateTimeSelectionToExpressionCompiler()
        )
        upper.setFilteredValue(compiler.getUpper())

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a fully custom format"
        compiler.compile("locale:(en)format:(EEEE HH'h')(Monday 15h)") == factory.and(
                lower.compile("lte:locale:(en)format:(EEEE HH'h')(Monday 15h)"),
                upper.compile("gte:locale:(en)format:(EEEE HH'h')(Monday 15h)")
        )
    }
}
