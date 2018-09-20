package org.liara.selection.natural

import spock.lang.Specification

class LongJPQLSelectionTranspilerSpecification
  extends Specification {
  def "it can parse long values" () {
    given: "a transpiler"
    final LongJPQLSelectionTranspiler transpiler = new LongJPQLSelectionTranspiler()

    expect: "it to be able to parse integer values"
    transpiler.transpile("gt:5.689").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:5.689").parameters == [
     "clause_0_value": 5L
    ]

    transpiler.transpile("gt:-107.487").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:-107.487").parameters == [
      "clause_0_value": -107L
    ]
  }
  def "it can add and subtract long values" () {
    given: "a transpiler"
    final LongJPQLSelectionTranspiler transpiler = new LongJPQLSelectionTranspiler()

    expect: "it to be able to add and subtract long values"
    transpiler.transpile("near:5.56+-3.15").clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
    transpiler.transpile("near:5.56+-3.15").parameters == [
      "clause_0_min": 2L,
      "clause_0_max": 8L
    ]
  }

  def "it throw an error if the value is out of range" () {
    given: "a transpiler"
    final LongJPQLSelectionTranspiler transpiler = new LongJPQLSelectionTranspiler()

    when: "we try to parse a value out of range"
    transpiler.transpile("gt:5487965847984688978976847986879.689")

    then: "we expect the transpiler to throw an error"
    thrown(Error)
  }
}
