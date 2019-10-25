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

package org.liara.test.selection.duration

import org.liara.expression.ExpressionFactory
import spock.lang.Specification

import java.time.Duration

class DurationSelectionToExpressionCompilerSpecification
        extends Specification {
    def "#compile can compile greater than clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a greater than clause"
        compiler.compile("gt:1y") == factory.greaterThan(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).toMillis())
        )

        compiler.compile("gt:15d") == factory.greaterThan(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(15).toMillis())
        )
    }

    def "#compile can compile greater than or equal clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile greater than or equal clause"
        compiler.compile("gte:1year+2day-3minutes") == factory.greaterThanOrEqual(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis())
        )
    }

    def "#compile can compile less than clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile less than clause"
        compiler.compile("lt:1year+2day-3minutes") == factory.lessThan(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis())
        )
    }

    def "#compile can compile less than or equal clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile less than or equal clause"
        compiler.compile("lte:1year+2day-3minutes") == factory.lessThanOrEqual(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis())
        )
    }

    def "#compile can compile equal clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile equal clauses"
        compiler.compile("1year+2day-3minutes") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis())
        )

        compiler.compile("eq:1year+2day-3minutes") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis())
        )
    }

    def "#compile can compile range clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile range clauses"
        compiler.compile("1years5days:2years") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(5).toMillis()),
                factory.nonnull(Duration.ofDays(365 * 2).toMillis())
        )

        compiler.compile("2years:1years5days") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(5).toMillis()),
                factory.nonnull(Duration.ofDays(365 * 2).toMillis())
        )

        compiler.compile("1years5days:and:2years") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(5).toMillis()),
                factory.nonnull(Duration.ofDays(365 * 2).toMillis())
        )

        compiler.compile("2years:and:1years5days") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(365).plusDays(5).toMillis()),
                factory.nonnull(Duration.ofDays(365 * 2).toMillis())
        )
    }

    def "#compile can compile near clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile near clauses"
        compiler.compile("near:5days+-2hours") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(5).minusHours(2).toMillis()),
                factory.nonnull(Duration.ofDays(5).plusHours(2).toMillis())
        )

        compiler.compile("near:5days:delta:2hours") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(5).minusHours(2).toMillis()),
                factory.nonnull(Duration.ofDays(5).plusHours(2).toMillis())
        )

        compiler.compile("near:5days:dt:2hours") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(5).minusHours(2).toMillis()),
                factory.nonnull(Duration.ofDays(5).plusHours(2).toMillis())
        )
    }

    def "#compile can compile negated clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile negated clauses"
        compiler.compile("not:near:5days+-2hours") == factory.not(compiler.compile("near:5days+-2hours"))
    }

    def "#compile can compile conjunction of clauses"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile conjunction of clauses"
        compiler.compile("gt:5days,lte:3years,not:10days:20days") == factory.and(Arrays.asList(
                compiler.compile("gt:5days"),
                compiler.compile("lte:3years"),
                compiler.compile("not:10days:20days")
        ))
    }

    def "#compile can compile disjunction of conjunctions"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a disjunction of clauses"
        compiler.compile("gt:5days;lte:3years,not:10days:20days;10days") == factory.or(Arrays.asList(
                compiler.compile("gt:5days"),
                compiler.compile("lte:3years,not:10days:20days"),
                compiler.compile("10days")
        ))
    }

    def "#compile can compile durations"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile durations"
        compiler.compile("129year") == compiler.compile("129years")
        compiler.compile("129years") == compiler.compile("129y")
        compiler.compile("129y") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(129 * 365).toMillis())
        )

        compiler.compile("382month") == compiler.compile("382months")
        compiler.compile("382months") == compiler.compile("382M")
        compiler.compile("382M") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(382 * 30).toMillis())
        )

        compiler.compile("281weeks") == compiler.compile("281week")
        compiler.compile("281week") == compiler.compile("281w")
        compiler.compile("281w") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(281 * 7).toMillis())
        )

        compiler.compile("136days") == compiler.compile("136day")
        compiler.compile("136day") == compiler.compile("136d")
        compiler.compile("136d") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofDays(136).toMillis())
        )

        compiler.compile("249hours") == compiler.compile("249hour")
        compiler.compile("249hour") == compiler.compile("249h")
        compiler.compile("249h") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofHours(249).toMillis())
        )

        compiler.compile("123minutes") == compiler.compile("123minute")
        compiler.compile("123minute") == compiler.compile("123m")
        compiler.compile("123m") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofMinutes(123).toMillis())
        )

        compiler.compile("456seconds") == compiler.compile("456second")
        compiler.compile("456second") == compiler.compile("456s")
        compiler.compile("456s") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofSeconds(456).toMillis())
        )

        compiler.compile("3149milliseconds") == compiler.compile("3149millisecond")
        compiler.compile("3149millisecond") == compiler.compile("3149ms")
        compiler.compile("3149ms") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Duration.ofMillis(3149).toMillis())
        )
    }

    def "#compile can compile complex durations"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile complex duration"
        compiler.compile("1month-20day3d+3hours-1year+6349874698hours") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(
                        Duration.ofDays(1 * 30)
                                .minusDays(20)
                                .plusDays(3)
                                .plusHours(3)
                                .minusDays(365)
                                .plusHours(6349874698).toMillis()
                )
        )
    }

    def "#compile throw an error if you use an invalid long value before a duration type"() {
        given: "a compiler"
        final DurationSelectionToExpressionCompiler compiler = (
                new DurationSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        when: "we try to compile a duration with an invalid long"
        compiler.compile("15489789465135668798731687984649846535498765months")

        then: " we expect the compiler to throw an error"
        thrown(Error.class)
    }
}
