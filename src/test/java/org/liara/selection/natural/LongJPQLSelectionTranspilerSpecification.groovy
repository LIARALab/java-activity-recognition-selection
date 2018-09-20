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
