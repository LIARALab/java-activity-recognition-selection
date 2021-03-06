/*
 * Copyright (C) 2019 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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

package org.liara.test.selection.natural

import org.liara.expression.ExpressionFactory
import spock.lang.Specification

class ByteSelectionToExpressionCompilerSpecification
  extends Specification {
  def "it can parse byte values" () {
      given: "a compiler"
      final ByteSelectionToExpressionCompiler compiler = new ByteSelectionToExpressionCompiler()

      and: "an expression factory"
      final ExpressionFactory factory = new ExpressionFactory()

    expect: "it to be able to parse byte values"
    compiler.compile("gt:5.689") == factory.greaterThan(
            compiler.getFilteredValue(),
            factory.nonnull((byte) 5)
    )

    compiler.compile("gt:-107.487") == factory.greaterThan(
            compiler.getFilteredValue(),
            factory.nonnull((byte) -107)
    )
  }
  def "it can add and subtract byte values" () {
      given: "a compiler"
      final ByteSelectionToExpressionCompiler compiler = new ByteSelectionToExpressionCompiler()

      and: "an expression factory"
      final ExpressionFactory factory = new ExpressionFactory()

    expect: "it to be able to add and subtract byte values"
    compiler.compile("near:5.56+-3.15") == factory.between(
            compiler.getFilteredValue(),
            factory.nonnull((byte) 2),
            factory.nonnull((byte) 8)
    )
  }

  def "it throw an error if the value is out of range" () {
      given: "a compiler"
      final ByteSelectionToExpressionCompiler compiler = new ByteSelectionToExpressionCompiler()

    when: "we try to parse a value out of range"
    compiler.compile("gt:5487965.689")

      then: "we expect the compiler to throw an error"
    thrown(Error)
  }
}
