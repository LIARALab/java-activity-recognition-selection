package org.liara.selection.natural

import spock.lang.Specification

class IntegerJPQLSelectionTranspilerSpecification
  extends Specification {
  def "it can parse integer values" () {
    given: "a transpiler"
    final IntegerJPQLSelectionTranspiler transpiler = new IntegerJPQLSelectionTranspiler()

    expect: "it to be able to parse integer values"
    transpiler.transpile("gt:5.689").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:5.689").parameters == [
     "clause_0_value": 5
    ]

    transpiler.transpile("gt:-107.487").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:-107.487").parameters == [
      "clause_0_value": -107
    ]
  }
  def "it can add and subtract integer values" () {
    given: "a transpiler"
    final IntegerJPQLSelectionTranspiler transpiler = new IntegerJPQLSelectionTranspiler()

    expect: "it to be able to add and subtract integer values"
    transpiler.transpile("near:5.56+-3.15").clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
    transpiler.transpile("near:5.56+-3.15").parameters == [
      "clause_0_min": 2,
      "clause_0_max": 8
    ]
  }

  def "it throw an error if the value is out of range" () {
    given: "a transpiler"
    final IntegerJPQLSelectionTranspiler transpiler = new IntegerJPQLSelectionTranspiler()

    when: "we try to parse a value out of range"
    transpiler.transpile("gt:54879656847986879.689")

    then: "we expect the transpiler to throw an error"
    thrown(Error)
  }
}
