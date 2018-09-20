/*
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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

package org.liara.selection.natural

import org.antlr.v4.runtime.Token
import org.liara.selection.jpql.JPQLQuery
import spock.lang.Specification

class NumberJPQLSelectionTranspilerSpecification
  extends Specification {
  NumberJPQLSelectionTranspiler<Double> createTranspiler() {
    final NumberJPQLSelectionTranspiler<Double> transpiler = new NumberJPQLSelectionTranspiler<Double>() {
      @Override
      protected Double parse(final Token token) {
        return Double.parseDouble(token.getText())
      }

      @Override
      protected Double add(final Double left, final Double right) {
        return left + right
      }

      @Override
      protected Double subtract(final Double left, final Double right) {
        return left - right
      }
    }

    return transpiler
  }

  def "it can transpile greater than clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile greater than clauses"
    transpiler.transpile("gt:5.689").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:5.689").parameters == [
      "clause_0_value": Double.parseDouble("5.689")
    ]

    transpiler.transpile("gt:203.487").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:203.487").parameters == [
      "clause_0_value": Double.parseDouble("203.487")
    ]
  }

  def "it can transpile greater than or equal clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile greater than or equal clauses"
    transpiler.transpile("gte:5.689") == JPQLQuery.query("(:this >= :clause_0_value)", [
      "clause_0_value": Double.parseDouble("5.689")
    ])

    transpiler.transpile("gte:203.487") == JPQLQuery.query("(:this >= :clause_0_value)", [
      "clause_0_value": Double.parseDouble("203.487")
    ])
  }

  def "it can transpile less than clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile less than clauses"
    transpiler.transpile("lt:5.689") == JPQLQuery.query("(:this < :clause_0_value)", [
      "clause_0_value": Double.parseDouble("5.689")
    ])

    transpiler.transpile("lt:203.487") == JPQLQuery.query("(:this < :clause_0_value)", [
      "clause_0_value": Double.parseDouble("203.487")
    ])
  }

  def "it can transpile less than or equal clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile less than or equal clauses"
    transpiler.transpile("lte:5.689") == JPQLQuery.query("(:this <= :clause_0_value)", [
      "clause_0_value": Double.parseDouble("5.689")
    ])

    transpiler.transpile("lte:203.487") == JPQLQuery.query("(:this <= :clause_0_value)", [
      "clause_0_value": Double.parseDouble("203.487")
    ])
  }

  def "it can transpile equal clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile equal clauses"
    transpiler.transpile("eq:5.689") == JPQLQuery.query("(:this = :clause_0_value)", [
      "clause_0_value": Double.parseDouble("5.689")
    ])

    transpiler.transpile("5.689") == JPQLQuery.query("(:this = :clause_0_value)", [
      "clause_0_value": Double.parseDouble("5.689")
    ])

    transpiler.transpile("eq:203.487") == JPQLQuery.query("(:this = :clause_0_value)", [
      "clause_0_value": Double.parseDouble("203.487")
    ])

    transpiler.transpile("203.487") == JPQLQuery.query("(:this = :clause_0_value)", [
      "clause_0_value": Double.parseDouble("203.487")
    ])
  }

  def "it can transpile range clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile range clauses"
    transpiler.transpile("5.689:62.489") == JPQLQuery.query(
      "(:this BETWEEN :clause_0_min AND :clause_0_max)",
      [
        "clause_0_min": Double.parseDouble("5.689"),
        "clause_0_max": Double.parseDouble("62.489")
      ]
    )

    transpiler.transpile("62.489:5.689") == JPQLQuery.query(
      "(:this BETWEEN :clause_0_min AND :clause_0_max)",
      [
        "clause_0_min": Double.parseDouble("5.689"),
        "clause_0_max": Double.parseDouble("62.489")
      ]
    )
  }

  def "it can transpile near clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile near clauses"
    transpiler.transpile("near:5.689+-62.489") == JPQLQuery.query(
      "(:this BETWEEN :clause_0_min AND :clause_0_max)",
      [
        "clause_0_min": Double.parseDouble("5.689") - Double.parseDouble("62.489"),
        "clause_0_max": Double.parseDouble("5.689") + Double.parseDouble("62.489")
      ]
    )

    transpiler.transpile("near:5.689+-62.489") == JPQLQuery.query(
      "(:this BETWEEN :clause_0_min AND :clause_0_max)",
      [
        "clause_0_min": Double.parseDouble("5.689") - Double.parseDouble("62.489"),
        "clause_0_max": Double.parseDouble("5.689") + Double.parseDouble("62.489")
      ]
    )

    transpiler.transpile("near:5.689:dt:62.489") == JPQLQuery.query(
      "(:this BETWEEN :clause_0_min AND :clause_0_max)",
      [
        "clause_0_min": Double.parseDouble("5.689") - Double.parseDouble("62.489"),
        "clause_0_max": Double.parseDouble("5.689") + Double.parseDouble("62.489")
      ]
    )

    transpiler.transpile("near:5.689:delta:62.489") == JPQLQuery.query(
      "(:this BETWEEN :clause_0_min AND :clause_0_max)",
      [
        "clause_0_min": Double.parseDouble("5.689") - Double.parseDouble("62.489"),
        "clause_0_max": Double.parseDouble("5.689") + Double.parseDouble("62.489")
      ]
    )
  }

  def "it can transpile negated clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile negated clauses"
    transpiler.transpile("not:near:5.689+-62.489") == JPQLQuery.query(
      "(NOT (:this BETWEEN :clause_0_min AND :clause_0_max))",
      [
        "clause_0_min": Double.parseDouble("5.689") - Double.parseDouble("62.489"),
        "clause_0_max": Double.parseDouble("5.689") + Double.parseDouble("62.489")
      ]
    )
  }

  def "it can transpile conjunction of clauses" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile conjunction of clauses"
    transpiler.transpile("not:gt:5.689,lt:3.56,lte:4.36") == JPQLQuery.query(
      "(NOT (:this > :clause_0_value) AND :this < :clause_1_value AND :this <= :clause_2_value)",
      [
        "clause_0_value": Double.parseDouble("5.689"),
        "clause_1_value": Double.parseDouble("3.56"),
        "clause_2_value": Double.parseDouble("4.36")
      ]
    )
  }

  def "it can transpile disjunction of conjunctions" () {
    given: "a transpiler"
    final NumberJPQLSelectionTranspiler<Double> transpiler = createTranspiler()

    expect: "it to be able to transpile disjunction of conjunctions"
    transpiler.transpile("not:gt:5.689,lt:3.56;lte:4.36") == JPQLQuery.query(
      "((NOT (:this > :clause_0_value) AND :this < :clause_1_value) OR (:this <= :clause_2_value))",
      [
        "clause_0_value": Double.parseDouble("5.689"),
        "clause_1_value": Double.parseDouble("3.56"),
        "clause_2_value": Double.parseDouble("4.36")
      ]
    )

    transpiler.transpile("not:gt:5.689;lt:3.56;lte:4.36") == JPQLQuery.query(
      "((NOT (:this > :clause_0_value)) OR (:this < :clause_1_value) OR (:this <= :clause_2_value))",
      [
        "clause_0_value": Double.parseDouble("5.689"),
        "clause_1_value": Double.parseDouble("3.56"),
        "clause_2_value": Double.parseDouble("4.36")
      ]
    )
  }
}
