package org.liara.selection.natural

import spock.lang.Specification

class ByteJPQLSelectionTranspilerSpecification
  extends Specification {
  def "it can parse byte values" () {
    given: "a transpiler"
    final ByteJPQLSelectionTranspiler transpiler = new ByteJPQLSelectionTranspiler()

    expect: "it to be able to parse byte values"
    transpiler.transpile("gt:5.689").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:5.689").parameters == [
     "clause_0_value": (byte) 5
    ]

    transpiler.transpile("gt:-107.487").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:-107.487").parameters == [
      "clause_0_value": (byte) -107
    ]
  }
  def "it can add and subtract byte values" () {
    given: "a transpiler"
    final ByteJPQLSelectionTranspiler transpiler = new ByteJPQLSelectionTranspiler()

    expect: "it to be able to add and subtract byte values"
    transpiler.transpile("near:5.56+-3.15").clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
    transpiler.transpile("near:5.56+-3.15").parameters == [
      "clause_0_min": (byte) 2,
      "clause_0_max": (byte) 8
    ]
  }

  def "it throw an error if the value is out of range" () {
    given: "a transpiler"
    final ByteJPQLSelectionTranspiler transpiler = new ByteJPQLSelectionTranspiler()

    when: "we try to parse a value out of range"
    transpiler.transpile("gt:5487965.689")

    then: "we expect the transpiler to throw an error"
    thrown(Error)
  }
}
