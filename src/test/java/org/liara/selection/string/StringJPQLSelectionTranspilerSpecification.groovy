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
    result.clause == "(:this REGEXP :clause_0_expression)"
    result.parameters == ["clause_0_expression": "ab/c?"]
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
      "AND NOT (:this REGEXP :clause_2_expression))"
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
      "AND :this REGEXP :clause_2_expression ",
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
      "(:this REGEXP :clause_1_expression ",
      "AND NOT (:this LIKE :clause_2_keyword)))"
    )

    result.parameters == [
      "clause_0_keyword": "%paul%",
      "clause_1_expression": "regexp",
      "clause_2_keyword": "%abc%",
    ]
  }
}
