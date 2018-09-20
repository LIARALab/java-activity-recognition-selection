package org.liara.selection.natural

import spock.lang.Specification

class BigDecimalJPQLSelectionTranspilerSpecification
  extends Specification {
  def "it can parse double values" () {
    given: "a transpiler"
    final BigDecimalJPQLSelectionTranspiler transpiler = new BigDecimalJPQLSelectionTranspiler()

    expect: "it to be able to parse big decimal values"
    transpiler.transpile("gt:5.689").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:5.689").parameters == [
     "clause_0_value": new BigDecimal("5.689")
    ]

    transpiler.transpile("gt:-107.487").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:-107.487").parameters == [
      "clause_0_value": new BigDecimal("-107.487")
    ]
  }
  def "it can add and subtract double values" () {
    given: "a transpiler"
    final BigDecimalJPQLSelectionTranspiler transpiler = new BigDecimalJPQLSelectionTranspiler()

    expect: "it to be able to add and subtract double values"
    transpiler.transpile("near:5.56+-3.15").clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
    transpiler.transpile("near:5.56+-3.15").parameters == [
      "clause_0_min": new BigDecimal("5.56").subtract(new BigDecimal("3.15")),
      "clause_0_max": new BigDecimal("5.56").add(new BigDecimal("3.15"))
    ]
  }
}
