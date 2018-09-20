package org.liara.selection.natural

import spock.lang.Specification

class DoubleJPQLSelectionTranspilerSpecification
  extends Specification {
  def "it can parse double values" () {
    given: "a transpiler"
    final DoubleJPQLSelectionTranspiler transpiler = new DoubleJPQLSelectionTranspiler()

    expect: "it to be able to parse double values"
    transpiler.transpile("gt:5.689").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:5.689").parameters == [
     "clause_0_value": 5.689d
    ]

    transpiler.transpile("gt:-107.487").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:-107.487").parameters == [
      "clause_0_value": -107.487d
    ]
  }
  def "it can add and subtract double values" () {
    given: "a transpiler"
    final DoubleJPQLSelectionTranspiler transpiler = new DoubleJPQLSelectionTranspiler()

    expect: "it to be able to add and subtract double values"
    transpiler.transpile("near:5.56+-3.15").clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
    transpiler.transpile("near:5.56+-3.15").parameters == [
      "clause_0_min": 5.56d - 3.15d,
      "clause_0_max": 5.56d + 3.15d
    ]
  }
}
