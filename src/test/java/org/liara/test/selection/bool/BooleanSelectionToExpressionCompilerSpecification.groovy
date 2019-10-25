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

package org.liara.test.selection.bool

import org.liara.data.primitive.Primitives
import org.liara.expression.ExpressionFactory
import spock.lang.Specification

class BooleanSelectionToExpressionCompilerSpecification extends Specification {
    def "#compile successfully compile equal true clauses"() {
        given: "a compiler"
        final BooleanSelectionToExpressionCompiler compiler = (
                new BooleanSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile equal true clauses"
        compiler.compile("true") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(true)
        )

        compiler.compile("TRUE") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(true)
        )

        compiler.compile("1") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(true)
        )

        compiler.compile("    ") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(true)
        )

        compiler.compile("eq:TRUE") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(true)
        )
    }

    def "#compile successfully compile equal false clauses"() {
        given: "a compiler"
        final BooleanSelectionToExpressionCompiler compiler = (
                new BooleanSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile equal false clauses"
        compiler.compile("false") == (
                factory.equal(compiler.getFilteredValue(), factory.nonnull(false))
        )

        compiler.compile("FALSE") == (
                factory.equal(compiler.getFilteredValue(), factory.nonnull(false))
        )

        compiler.compile("0") == (
                factory.equal(compiler.getFilteredValue(), factory.nonnull(false))
        )

        compiler.compile("eq:FALSE") == (
                factory.equal(compiler.getFilteredValue(), factory.nonnull(false))
        )
    }

    def "#compile successfully compile equal null closes"() {
        given: "a compiler"
        final BooleanSelectionToExpressionCompiler compiler = (
                new BooleanSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile equal null clauses"
        compiler.compile("null") == factory.equal(
                compiler.getFilteredValue(),
                factory.constant(Primitives.NULLABLE_BOOLEAN, null)
        )

        compiler.compile("NULL") == factory.equal(
                compiler.getFilteredValue(),
                factory.constant(Primitives.NULLABLE_BOOLEAN, null)
        )

        compiler.compile("eq:NULL") == factory.equal(
                compiler.getFilteredValue(),
                factory.constant(Primitives.NULLABLE_BOOLEAN, null)
        )
    }

    def "#compile successfully compile negated closes"() {
        given: "a compiler"
        final BooleanSelectionToExpressionCompiler compiler = (
                new BooleanSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile negated clauses"
        compiler.compile("not:eq:null") == factory.not(compiler.compile("eq:null"))
        compiler.compile("not:NULL") == factory.not(compiler.compile("NULL"))
    }

    def "#compile successfully compile different closes"() {
        given: "a compiler"
        final BooleanSelectionToExpressionCompiler compiler = (
                new BooleanSelectionToExpressionCompiler()
        )

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "it to be able to compile negated clauses"
        compiler.compile("true") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(true)
        )

        compiler.compile("0") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull(false)
        )

        compiler.compile("NULL") == factory.equal(
                compiler.getFilteredValue(),
                factory.constant(Primitives.NULLABLE_BOOLEAN, null)
        )
    }
}
