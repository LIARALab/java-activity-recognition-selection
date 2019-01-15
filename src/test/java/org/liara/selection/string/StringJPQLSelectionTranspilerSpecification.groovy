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

package org.liara.selection.string

import org.liara.selection.jpql.JPQLQuery
import spock.lang.Specification

class StringJPQLSelectionTranspilerSpecification
  extends Specification
{
  def "it can transpile keywords clauses" () {
    given: "a transpiler"
    final StringJPQLSelectionTranspiler transpiler = new StringJPQLSelectionTranspiler()

    when: "we transpile a keyword clause expression"
    final JPQLQuery result = transpiler.transpile("jean jacques  louis\n\rmarc   \npaul,valère")

    then: "we expect it to be able to transpile keywords clauses"
    result.clause == String.join(
      "",
      "(:this LIKE :clause_0_keyword ",
      "AND :this LIKE :clause_1_keyword ",
      "AND :this LIKE :clause_2_keyword ",
      "AND :this LIKE :clause_3_keyword ",
      "AND :this LIKE :clause_4_keyword ",
      "AND :this LIKE :clause_5_keyword)",
    )

    result.parameters == [
      "clause_0_keyword": "%jean%",
      "clause_1_keyword": "%jacques%",
      "clause_2_keyword": "%louis%",
      "clause_3_keyword": "%marc%",
      "clause_4_keyword": "%paul%",
      "clause_5_keyword": "%valère%"
    ]
  }

  def "it can transpile regular expression clauses" () {
    given: "a transpiler"
    final StringJPQLSelectionTranspiler transpiler = new StringJPQLSelectionTranspiler()

    when: "we transpile a regular expression"
    final JPQLQuery result = transpiler.transpile("/ab\\/c?/")

    then: "we expect it to be able to transpile the regular expression"
    result.clause == "(function('regexp', :this, :clause_0_expression) = 1)"
    result.parameters == ["clause_0_expression": "ab/c?"]
  }

  def "it can transpile equal clauses" () {
    given: "a transpiler"
    final StringJPQLSelectionTranspiler transpiler = new StringJPQLSelectionTranspiler()

    when: "we transpile an equal clause"
    final JPQLQuery result = transpiler.transpile("eq:not:not:eq:pouet")

    then: "we expect it to be able to transpile the equal clause"
    result.clause == "(:this = :clause_0_keyword)"
    result.parameters == ["clause_0_keyword": "pouet"]
  }

  def "it can transpile exact match clauses" () {
    given: "a transpiler"
    final StringJPQLSelectionTranspiler transpiler = new StringJPQLSelectionTranspiler()

    when: "we transpile an exact match clause"
    final JPQLQuery result = transpiler.transpile("\"an exact \\\"match\\\", expression\"")

    then: "we expect it to be able to transpile the exact match clause"
    result.clause == "(:this LIKE :clause_0_keyword)"
    result.parameters == ["clause_0_keyword": "%an exact \"match\", expression%"]
  }

  def "it can transpile negation of clauses" () {
    given: "a transpiler"
    final StringJPQLSelectionTranspiler transpiler = new StringJPQLSelectionTranspiler()

    when: "we transpile negation of clauses"
    final JPQLQuery result = transpiler.transpile("not:paul,not:\"pl an\",not:/regexp/")

    then: "we expect it to be able to transpile the negation"
    result.clause == String.join(
      "",
      "(NOT (:this LIKE :clause_0_keyword) ",
      "AND NOT (:this LIKE :clause_1_keyword) ",
      "AND NOT (function('regexp', :this, :clause_2_expression) = 1))"
    )

    result.parameters == [
      "clause_0_keyword": "%paul%",
      "clause_1_keyword": "%pl an%",
      "clause_2_expression": "regexp"
    ]
  }

  def "it can transpile conjunction of clauses" () {
    given: "a transpiler"
    final StringJPQLSelectionTranspiler transpiler = new StringJPQLSelectionTranspiler()

    when: "we transpile a conjunction of clauses"
    final JPQLQuery result = transpiler.transpile("paul \"pl an\",/regexp/ not:abc")

    then: "we expect it to be able to transpile the conjunction"
    result.clause == String.join(
      "",
      "(:this LIKE :clause_0_keyword ",
      "AND :this LIKE :clause_1_keyword ",
      "AND function('regexp', :this, :clause_2_expression) = 1 ",
      "AND NOT (:this LIKE :clause_3_keyword))"
    )
    result.parameters == [
      "clause_0_keyword": "%paul%",
      "clause_1_keyword": "%pl an%",
      "clause_2_expression": "regexp",
      "clause_3_keyword": "%abc%",
    ]
  }

  def "it can transpile disjunction of filters" () {
    given: "a transpiler"
    final StringJPQLSelectionTranspiler transpiler = new StringJPQLSelectionTranspiler()

    when: "we transpile a disjunction of filters"
    final JPQLQuery result = transpiler.transpile("paul;/regexp/ not:abc")

    then: "we expect it to be able to transpile the disjunction"
    result.clause == String.join(
      "",
      "((:this LIKE :clause_0_keyword) OR ",
      "(function('regexp', :this, :clause_1_expression) = 1 ",
      "AND NOT (:this LIKE :clause_2_keyword)))"
    )

    result.parameters == [
      "clause_0_keyword": "%paul%",
      "clause_1_expression": "regexp",
      "clause_2_keyword": "%abc%",
    ]
  }
}
