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

package org.liara.test.selection.natural


import org.liara.expression.ExpressionFactory
import spock.lang.Specification

class NumberSelectionToExpressionCompilerSpecification
        extends Specification {
    NumberSelectionToExpressionCompiler<Double> createCompiler() {
        return new DoubleSelectionToExpressionCompiler()
    }

    def "it can compile greater than clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile greater than clauses"
        compiler.compile("gt:5.689") == factory.greaterThan(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689"))
        )

        compiler.compile("gt:203.487") == factory.greaterThan(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("203.487"))
        )
    }

    def "it can compile greater than or equal clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile greater than or equal clauses"
        compiler.compile("gte:5.689") == factory.greaterThanOrEqual(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689"))
        )

        compiler.compile("gte:203.487") == factory.greaterThanOrEqual(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("203.487"))
        )
    }

    def "it can compile less than clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile less than clauses"
        compiler.compile("lt:5.689") == factory.lessThan(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689"))
        )

        compiler.compile("lt:203.487") == factory.lessThan(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("203.487"))
        )
    }

    def "it can compile less than or equal clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile less than or equal clauses"
        compiler.compile("lte:5.689") == factory.lessThanOrEqual(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689"))
        )

        compiler.compile("lte:203.487") == factory.lessThanOrEqual(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("203.487"))
        )
    }

    def "it can compile equal clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile equal clauses"
        compiler.compile("eq:5.689") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689"))
        )

        compiler.compile("5.689") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689"))
        )

        compiler.compile("eq:203.487") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("203.487"))
        )

        compiler.compile("203.487") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("203.487"))
        )
    }

    def "it can compile range clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile range clauses"
        compiler.compile("5.689:62.489") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689")),
                factory.nonnull(Double.parseDouble("62.489"))
        )

        compiler.compile("62.489:5.689") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689")),
                factory.nonnull(Double.parseDouble("62.489"))
        )
    }

    def "it can compile near clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile near clauses"
        compiler.compile("near:5.689+-62.489") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689") - Double.parseDouble("62.489")),
                factory.nonnull(Double.parseDouble("5.689") + Double.parseDouble("62.489"))
        )

        compiler.compile("near:5.689:dt:62.489") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689") - Double.parseDouble("62.489")),
                factory.nonnull(Double.parseDouble("5.689") + Double.parseDouble("62.489"))
        )

        compiler.compile("near:5.689:delta:62.489") == factory.between(
                compiler.getFilteredValue(),
                factory.nonnull(Double.parseDouble("5.689") - Double.parseDouble("62.489")),
                factory.nonnull(Double.parseDouble("5.689") + Double.parseDouble("62.489"))
        )
    }

    def "it can compile negated clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile negated clauses"
        compiler.compile("not:near:5.689+-62.489") == factory.not(compiler.compile("near:5.689+-62.489"))
    }

    def "it can compile conjunction of clauses"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile conjunction of clauses"
        compiler.compile("not:gt:5.689,lt:3.56,lte:4.36") == factory.and(Arrays.asList(
                compiler.compile("not:gt:5.689"),
                compiler.compile("lt:3.56"),
                compiler.compile("lte:4.36")
        ))
    }

    def "it can compile disjunction of conjunctions"() {
        given: "a compiler"
        final NumberSelectionToExpressionCompiler<Double> compiler = createCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile disjunction of conjunctions"
        compiler.compile("not:gt:5.689,lt:3.56;lte:4.36") == factory.or(
                compiler.compile("not:gt:5.689,lt:3.56"),
                compiler.compile("lte:4.36")
        )

        compiler.compile("not:gt:5.689;lt:3.56;lte:4.36") == factory.or(Arrays.asList(
                compiler.compile("not:gt:5.689"),
                compiler.compile("lt:3.56"),
                compiler.compile("lte:4.36")
        ))
    }
}
