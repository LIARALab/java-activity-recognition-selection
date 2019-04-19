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

package org.liara.selection.duration

import org.liara.selection.jpql.JPQLQuery
import spock.lang.Specification

import java.time.Duration

class DurationJPQLSelectionTranspilerSpecification
  extends Specification {

  def "it can transpile greater than clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a greater than clause"
    final JPQLQuery result = transpiler.transpile("gt:1y")

    then: " we expect the transpiler to be able to transpile the greater than clause"
    result.clause == "(:this > :clause_0_value)"
    result.parameters == [
      "clause_0_value": Duration.ofDays(365).toMillis()
    ]
  }

  def "it can transpile greater than or equal clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a greater than or equal clause"
    final JPQLQuery result = transpiler.transpile("gte:1year+2day-3minutes")

    then: " we expect the transpiler to be able to transpile the greater than or equal clause"
    result.clause == "(:this >= :clause_0_value)"
    result.parameters == [
      "clause_0_value": Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis()
    ]
  }

  def "it can transpile less than clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a less than clause"
    final JPQLQuery result = transpiler.transpile("lt:1year+2day-3minutes")

    then: " we expect the transpiler to be able to transpile the less than clause"
    result.clause == "(:this < :clause_0_value)"
    result.parameters == [
      "clause_0_value": Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis()
    ]
  }

  def "it can transpile less than or equal clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a less than or equal clause"
    final JPQLQuery result = transpiler.transpile("lte:1year+2day-3minutes")

    then: " we expect the transpiler to be able to transpile the less than or equal clause"
    result.clause == "(:this <= :clause_0_value)"
    result.parameters == [
      "clause_0_value": Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis()
    ]
  }

  def "it can transpile equal clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile an equal clause"
    final JPQLQuery[] results = [
      transpiler.transpile("1year+2day-3minutes"),
      transpiler.transpile("eq:1year+2day-3minutes")
    ] as JPQLQuery[]

    then: " we expect the transpiler to be able to transpile the less than or equal clause"
    for (final JPQLQuery result : results) {
      result.clause == "(:this <= :clause_0_value)"
      result.parameters == [
        "clause_0_value": Duration.ofDays(365).plusDays(2).minusMinutes(3).toMillis()
      ]
    }
  }

  def "it can transpile range clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a range clause"
    final JPQLQuery[] results = [
      transpiler.transpile("1years5days:2years"),
      transpiler.transpile("2years:1years5days"),
      transpiler.transpile("1years5days:and:2years"),
      transpiler.transpile("2years:and:1years5days")
    ] as JPQLQuery[]

    then: " we expect the transpiler to be able to transpile the range clause"
    for (final JPQLQuery result : results) {
      result.clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
      result.parameters == [
        "clause_0_min": Duration.ofDays(365).plusDays(5).toMillis(),
        "clause_0_max": Duration.ofDays(365 * 2).toMillis()
      ]
    }
  }

  def "it can transpile near clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a near clause"
    final JPQLQuery[] results = [
      transpiler.transpile("near:5days+-2hours"),
      transpiler.transpile("near:5days:delta:2hours"),
      transpiler.transpile("near:5days:dt:2hours")
    ] as JPQLQuery[]

    then: " we expect the transpiler to be able to transpile the near clause"
    for (final JPQLQuery result : results) {
      result.clause == "(:this BETWEEN :clause_0_min AND :clause_0_max)"
      result.parameters == [
        "clause_0_min": Duration.ofDays(5).plusHours(2).toMillis(),
        "clause_0_max": Duration.ofDays(5).minusHours(2).toMillis()
      ]
    }
  }

  def "it can transpile negated clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a negated clause"
    final JPQLQuery result = transpiler.transpile("not:near:5days+-2hours")

    then: " we expect the transpiler to be able to transpile the near clause"
    result.clause == "(NOT (:this BETWEEN :clause_0_min AND :clause_0_max))"
    result.parameters == [
      "clause_0_min": Duration.ofDays(5).minusHours(2).toMillis(),
      "clause_0_max": Duration.ofDays(5).plusHours(2).toMillis()
    ]
  }

  def "it can transpile conjunction of clauses" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a conjunction of clause"
    final JPQLQuery result = transpiler.transpile("gt:5days,lte:3years,not:10days:20days")

    then: " we expect the transpiler to be able to transpile the conjunction of clauses"
    result.clause == String.join(
      "",
      "(:this > :clause_0_value AND ",
      ":this <= :clause_1_value AND ",
      "NOT (:this BETWEEN :clause_2_min AND :clause_2_max))"
    )

    result.parameters == [
      "clause_0_value": Duration.ofDays(5).toMillis(),
      "clause_1_value": Duration.ofDays(365 * 3).toMillis(),
      "clause_2_min": Duration.ofDays(10).toMillis(),
      "clause_2_max": Duration.ofDays(20).toMillis()
    ]
  }

  def "it can transpile disjunction of conjunctions" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a disjunction of conjunctions"
    final JPQLQuery result = transpiler.transpile("gt:5days;lte:3years,not:10days:20days;10years")

    then: " we expect the transpiler to be able to transpile the disjunction of conjunctions"
    result.clause == String.join(
      "",
      "(:this > :clause_0_value) OR ",
      "(:this <= :clause_1_value AND ",
      "NOT (:this BETWEEN :clause_2_min AND :clause_2_max)) OR ",
      "(:this = :clause_3_value)"
    )

    result.parameters == [
      "clause_0_value": Duration.ofDays(5).toMillis(),
      "clause_1_value": Duration.ofDays(365 * 3).toMillis(),
      "clause_2_min": Duration.ofDays(10).toMillis(),
      "clause_2_max": Duration.ofDays(20).toMillis(),
      "clause_3_value": Duration.ofDays(365 * 10).toMillis()
    ]
  }

  def "it can transpile durations" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile durations"
    final Map<Duration, JPQLQuery[]> results = [
      (Duration.ofDays(129 * 365)): [
        transpiler.transpile("129year"),
        transpiler.transpile("129years"),
        transpiler.transpile("129y")
      ] as JPQLQuery[],
      (Duration.ofDays(382 * 30)): [
        transpiler.transpile("382month"),
        transpiler.transpile("382months"),
        transpiler.transpile("382M")
      ] as JPQLQuery[],
      (Duration.ofDays(281 * 7)): [
        transpiler.transpile("281weeks"),
        transpiler.transpile("281week"),
        transpiler.transpile("281w")
      ] as JPQLQuery[],
      (Duration.ofDays(136)): [
        transpiler.transpile("136days"),
        transpiler.transpile("136day"),
        transpiler.transpile("136d")
      ] as JPQLQuery[],
      (Duration.ofHours(249)): [
        transpiler.transpile("249hours"),
        transpiler.transpile("249hour"),
        transpiler.transpile("249h")
      ] as JPQLQuery[],
      (Duration.ofMinutes(123)): [
        transpiler.transpile("123minutes"),
        transpiler.transpile("123minute"),
        transpiler.transpile("123m")
      ] as JPQLQuery[],
      (Duration.ofSeconds(456)): [
        transpiler.transpile("456seconds"),
        transpiler.transpile("456second"),
        transpiler.transpile("456s")
      ] as JPQLQuery[],
      (Duration.ofMillis(3149)): [
        transpiler.transpile("3149milliseconds"),
        transpiler.transpile("3149millisecond"),
        transpiler.transpile("3149ms")
      ] as JPQLQuery[]
    ]

    then: " we expect the transpiler to be able to transpile durarions"
    for (final Map.Entry<Duration, JPQLQuery[]> entry : results.entrySet()) {
      for (final JPQLQuery resultQuery : entry.value) {
        resultQuery.parameters['clause_0_value'] == entry.key.toMillis()
      }
    }
  }

  def "it can transpile complex durations" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile complex durations"
    final JPQLQuery result = transpiler.transpile("1month-20day3d+3hours-1year+6349874698hours")

    then: " we expect the transpiler to be able to transpile complex durarions"
    result.parameters['clause_0_value'] == Duration.ofDays(1 * 30)
                                                   .minusDays(20)
                                                   .plusDays(3)
                                                   .plusHours(3)
                                                   .minusDays(365)
                                                   .plusHours(6349874698).toMillis()
  }

  def "it throw an error if you use an invalid long value before a duration type" () {
    given: "a transpiler"
    final DurationJPQLSelectionTranspiler transpiler = new DurationJPQLSelectionTranspiler()

    when: "we try to transpile a duration with an invalid long"
    final JPQLQuery result = transpiler.transpile("15489789465135668798731687984649846535498765months")

    then: " we expect the transpiler to throw an error"
    thrown(Error.class)
  }
}
