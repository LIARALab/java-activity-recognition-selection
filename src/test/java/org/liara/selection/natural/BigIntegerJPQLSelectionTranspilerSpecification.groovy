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
