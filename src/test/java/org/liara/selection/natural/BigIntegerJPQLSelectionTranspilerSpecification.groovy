package org.liara.selection.natural

import spock.lang.Specification

class BigIntegerJPQLSelectionTranspilerSpecification
  extends Specification {
  def "it can parse big integer values" () {
    given: "a transpiler"
    final BigIntegerJPQLSelectionTranspiler transpiler = new BigIntegerJPQLSelectionTranspiler()

    expect: "it to be able to parse big integer values"
    transpiler.transpile("gt:568764898798764867987.689").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:568764898798764867987.689").parameters == [
     "clause_0_value": new BigInteger("568764898798764867987")
    ]

    transpiler.transpile("gt:-107687641498846786466878756465.487").clause == "(:this > :clause_0_value)"
    transpiler.transpile("gt:-107687641498846786466878756465.487").parameters == [
      "clause_0_value": new BigInteger("-107687641498846786466878756465")
    ]
  }
  def "it can add and subtract big integer values" () {
    given: "a transpiler"
    final BigIntegerJPQLSelectionTranspiler transpiler = new BigIntegerJPQLSelectionTranspiler()

    expect: "it to be able to add and subtract big integer values"
    transpiler.transpile("near:5.56+-3.15").clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
    transpiler.transpile("near:5.56+-3.15").parameters == [
      "clause_0_min": new BigInteger("2"),
      "clause_0_max": new BigInteger("8")
    ]
  }
}
