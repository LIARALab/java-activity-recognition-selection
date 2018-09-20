package org.liara.selection.natural

import spock.lang.Specification

class FloatJPQLSelectionTranspilerSpecification
  extends Specification {
  def "it can parse float values" () {
    given: "a transpiler"
    final FloatJPQLSelectionTranspiler transpiler = new FloatJPQLSelectionTranspiler()

    expect: "it to be able to parse float values"
    transpiler.transpile("gt:5.689").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:5.689").parameters == [
     "clause_0_value": 5.689f
    ]

    transpiler.transpile("gt:-107.487").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:-107.487").parameters == [
      "clause_0_value": -107.487f
    ]
  }
  def "it can add and subtract float values" () {
    given: "a transpiler"
    final FloatJPQLSelectionTranspiler transpiler = new FloatJPQLSelectionTranspiler()

    expect: "it to be able to add and subtract float values"
    transpiler.transpile("near:5.56+-3.15").clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
    transpiler.transpile("near:5.56+-3.15").parameters == [
      "clause_0_min": 5.56f - 3.15f,
      "clause_0_max": 5.56f + 3.15f
    ]
  }
}
