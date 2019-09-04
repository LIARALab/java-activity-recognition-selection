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

package org.liara.test.selection.string

import org.liara.expression.ExpressionFactory
import spock.lang.Specification

class StringSelectionToExpressionCompilerSpecification
        extends Specification {
    def "#compile can compile keywords clauses"() {
        given: "a compiler"
        final StringSelectionToExpressionCompiler compiler = new StringSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile keywords clauses"
        compiler.compile("jean") == factory.like(
                compiler.getFilteredValue(),
                factory.nonnull("%jean%")
        )
    }

    def "#compile can compile regular expression clauses"() {
        given: "a compiler"
        final StringSelectionToExpressionCompiler compiler = new StringSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile regular expression clauses"
        compiler.compile("/ab\\/c?/") == factory.regexp(
                compiler.getFilteredValue(),
                factory.nonnull("ab/c?")
        )
    }

    def "#compile can compile equal clauses"() {
        given: "a compiler"
        final StringSelectionToExpressionCompiler compiler = new StringSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile equal clauses"
        compiler.compile("eq:kiwi") == factory.equal(
                compiler.getFilteredValue(),
                factory.nonnull("kiwi")
        )
    }

    def "#compile can compile exact match clauses"() {
        given: "a compiler"
        final StringSelectionToExpressionCompiler compiler = new StringSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile an exact match clause"
        compiler.compile("\"an exact \\\"match\\\", expression\"") == factory.like(
                compiler.getFilteredValue(),
                factory.nonnull("%an exact \"match\", expression%")
        )
    }

    def "#compile can compile negation of clauses"() {
        given: "a compiler"
        final StringSelectionToExpressionCompiler compiler = new StringSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile negation of clauses"
        compiler.compile("not:paul") == factory.not(compiler.compile("paul"))
        compiler.compile("not:\"pl an\"") == factory.not(compiler.compile("\"pl an\""))
        compiler.compile("not:/regexp/") == factory.not(compiler.compile("/regexp/"))
    }

    def "#compile can compile conjunction of clauses"() {
        given: "a compiler"
        final StringSelectionToExpressionCompiler compiler = new StringSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a conjunction of clauses"
        compiler.compile("paul \"pl an\",/regexp/ not:abc") == factory.and(Arrays.asList(
                compiler.compile("paul"),
                compiler.compile("\"pl an\""),
                compiler.compile("/regexp/"),
                compiler.compile("not:abc")
        ))
    }

    def "#compile can compile disjunction of filters"() {
        given: "a compiler"
        final StringSelectionToExpressionCompiler compiler = new StringSelectionToExpressionCompiler()

        and: "an expression factory"
        final ExpressionFactory factory = new ExpressionFactory()

        expect: "to be able to compile a conjunction of clauses"
        compiler.compile("paul;abc /regexp/") == factory.or(
                compiler.compile("paul"),
                compiler.compile("abc /regexp/")
        )
    }
}
