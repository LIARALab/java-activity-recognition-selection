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

package org.liara.selection.datetime

import org.liara.selection.jpql.JPQLQuery
import spock.lang.Specification

class DatetimeJPQLSelectionTranspilerSpecification
  extends Specification {

  def setup () {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  }

  def "it can transpile greater than clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a greater than clause"
    final JPQLQuery result = transpiler.transpile("gt:(2018-12-10T15:20:30+01:00[Europe/Paris])")

    then: " we expect the transpiler to be able to transpile the greater than clause"
    result.clause == "(CONVERT_TZ(:this, 'UTC', 'Europe/Paris') > :clause_0_value)"
    result.parameters == [
      "clause_0_value": '2018-12-10T15:20:30'
    ]
  }

  def "it can transpile greater than or equal clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a greater than or equal clause"
    final JPQLQuery result = transpiler.transpile("gte:(2018-12-10T15:20:30+01:00[Europe/Paris])")

    then: " we expect the transpiler to be able to transpile the greater than or equal clause"
    result.clause == "(CONVERT_TZ(:this, 'UTC', 'Europe/Paris') >= :clause_0_value)"
    result.parameters == [
      "clause_0_value": '2018-12-10T15:20:30'
    ]
  }

  def "it can transpile less than clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a less than clause"
    final JPQLQuery result = transpiler.transpile("lt:(2018-12-10T15:20:30+01:00[Europe/Paris])")

    then: " we expect the transpiler to be able to transpile the less than clause"
    result.clause == "(CONVERT_TZ(:this, 'UTC', 'Europe/Paris') < :clause_0_value)"
    result.parameters == [
      "clause_0_value": '2018-12-10T15:20:30'
    ]
  }

  def "it can transpile less than or equal clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a less than or equal clause"
    final JPQLQuery result = transpiler.transpile("lte:(2018-12-10T15:20:30+01:00[Europe/Paris])")

    then: " we expect the transpiler to be able to transpile the less than or equal clause"
    result.clause == "(CONVERT_TZ(:this, 'UTC', 'Europe/Paris') <= :clause_0_value)"
    result.parameters == [
      "clause_0_value": '2018-12-10T15:20:30'
    ]
  }

  def "it can transpile equal clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile an equal clause"
    final JPQLQuery[] results = [
      transpiler.transpile("eq:(2018-12-10T15:20:30+01:00[Europe/Paris])"),
      transpiler.transpile("(2018-12-10T15:20:30+01:00[Europe/Paris])")
    ] as JPQLQuery[]

    then: " we expect the transpiler to be able to transpile the equal clause"
    for (final JPQLQuery result : results) {
      result.clause == "(CONVERT_TZ(:this, 'UTC', 'Europe/Paris') = :clause_0_value)"
      result.parameters == [
        "clause_0_value": '2018-12-10T15:20:30'
      ]
    }
  }

  def "it can transpile range clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a range clause"
    final JPQLQuery[] results = [
      transpiler.transpile(
        "(2018-12-16T18:10:20-05:00[America/New_York]):(2018-12-16T16:20:30+01:00[Europe/Paris])"
      ),
      transpiler.transpile(
        "(2018-12-16T16:20:30+01:00[Europe/Paris]):(2018-12-16T18:10:20-05:00[America/New_York])"
      )
    ] as JPQLQuery[]

    then: " we expect the transpiler to be able to transpile the range clause"
    for (final JPQLQuery result : results) {
      result.clause == "(CONVERT_TZ(:this, 'UTC', 'America/New-York') >= :clause_0_min AND CONVERT_TZ(:this, 'UTC', 'Europe/Paris') <= :clause_0_max)"
      result.parameters == [
        "clause_0_min": '2018-12-16T18:10:20',
        "clause_0_max": '2018-12-16T15:20:30'
      ]
    }
  }

  def "it can transpile negation of clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a less than or equal clause"
    final JPQLQuery[] results = [
      transpiler.transpile("not:eq:(2018-12-10T15:20:30+01:00[Europe/Paris])"),
      transpiler.transpile(
        "not:(2018-12-16T18:10:20-05:00[America/New_York]):(2018-12-16T11:20:30+01:00[Europe/Paris])"
      )
    ] as JPQLQuery[]

    then: " we expect the transpiler to be able to transpile the less than or equal clause"
    results[0].clause == "(NOT (CONVERT_TZ(:this, 'UTC', 'Europe/Paris') = :clause_0_value))"
    results[0].parameters == [
      "clause_0_value": '2018-12-10T15:20:30'
    ]
    results[
      1
    ].clause == "(NOT (CONVERT_TZ(:this, 'UTC', 'Europe/Paris') >= :clause_0_min AND CONVERT_TZ(:this, 'UTC', 'America/New_York') <= :clause_0_max))"
    results[1].parameters == [
      "clause_0_min": '2018-12-16T11:20:30',
      "clause_0_max": '2018-12-16T18:10:20'
    ]
  }

  def "it can transpile conjunction of clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a conjunction of clauses"
    final JPQLQuery result = transpiler.transpile(
      "not:eq:(2018-12-10T15:20:30+01:00[Europe/Paris]),lt:(2018-12-20T15:20:30+01:00[Europe/Paris])," +
        "gt:(2018-12-03T15:20:30+01:00[Europe/Paris])"
    )

    then: "we expect the transpiler to be able to transpile the conjunction of clauses"
    result.clause == "(NOT (CONVERT_TZ(:this, 'UTC', 'Europe/Paris') = :clause_0_value) AND CONVERT_TZ(:this, 'UTC', 'Europe/Paris') < :clause_1_value AND CONVERT_TZ(:this, 'UTC', 'Europe/Paris') > :clause_2_value)"
    result.parameters == [
      "clause_0_value": '2018-12-10T15:20:30',
      "clause_1_value": '2018-12-20T15:20:30',
      "clause_2_value": '2018-12-03T15:20:30'
    ]
  }

  def "it can transpile disjunction of clauses" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a disjunction of clauses"
    final JPQLQuery result = transpiler.transpile(
      "not:eq:(2018-12-10T15:20:30+01:00[Europe/Paris]),lt:(2018-12-20T15:20:30+01:00[Europe/Paris]);" +
        "gt:(2018-12-03T15:20:30+01:00[Europe/Paris])"
    )

    then: "we expect the transpiler to be able to transpile the disjunction of clauses"
    result.clause == "(NOT (CONVERT_TZ(:this, 'UTC', 'Europe/Paris') = :clause_0_value) AND CONVERT_TZ(:this, 'UTC', 'Europe/Paris') < :clause_1_value) OR (CONVERT_TZ(:this, 'UTC', 'Europe/Paris') > :clause_2_value)"
    result.parameters == [
      "clause_0_value": '2018-12-10T15:20:30',
      "clause_1_value": '2018-12-20T15:20:30',
      "clause_2_value": '2018-12-03T15:20:30'
    ]
  }

  def "it can transpile datetime" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a datetime"
    final JPQLQuery[] result = [
      transpiler.transpile("(2018-12-10T15:20:30+01:00[Europe/Paris])"),
      transpiler.transpile("(2018-12-10T15:20:30+00:00[UTC])")
    ]

    then: "we expect the transpiler to be able to transpile the datetime"
    result[0].clause == "(CONVERT_TZ(:this, 'UTC', 'Europe/Paris') = :clause_0_value)"
    result[0].parameters == [
      "clause_0_value": '2018-12-10T15:20:30'
    ]

    result[1].clause == "(:this = :clause_0_value)"
    result[1].parameters == [
      "clause_0_value": '2018-12-10T15:20:30'
    ]
  }

  def "it can transpile date" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a date"
    final JPQLQuery[] result = [
      transpiler.transpile("format:(yyyy-MM-dd)(2018-12-10)"),
      transpiler.transpile("format:(yyyy-MM-ddX)(2018-12-10+05)")
    ]

    then: "we expect the transpiler to be able to transpile the date"
    result[0].clause == "(DATE_FORMAT(:this, '%Y-%m-%d') = :clause_0_value)"
    result[0].parameters == [
      "clause_0_value": '2018-12-10'
    ]
    result[1].clause == "(DATE_FORMAT(CONVERT_TZ(:this, 'UTC', '+05:00'), '%Y-%m-%d') = :clause_0_value)"
    result[1].parameters == [
      "clause_0_value": '2018-12-10'
    ]
  }

  def "it can transpile time" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a time"
    final JPQLQuery[] result = [
      transpiler.transpile("format:(HH:mm:ss)(15:20:30)"),
      transpiler.transpile("format:(HH:mm:ssX)(15:20:30+05)"),
    ]

    then: "we expect the transpiler to be able to transpile the time"
    result[0].clause == "(DATE_FORMAT(:this, '%H:%i:%s.%f') = :clause_0_value)"
    result[0].parameters == [
      "clause_0_value": '15:20:30'
    ]
    result[1].clause == "(DATE_FORMAT(CONVERT_TZ(:this, 'UTC', '+05:00'), '%H:%i:%s.%f') = :clause_0_value)"
    result[1].parameters == [
      "clause_0_value": '15:20:30'
    ]
  }

  def "it can transpile fully custom format" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a fully custom format"
    final JPQLQuery[] result = [
      transpiler.transpile("locale:(en)format:(EEEE HH'h')(Monday 15h)"),
      transpiler.transpile("locale:(en)format:(EEEE HH'h'X)(Monday 15h+05)")
    ]

    then: "we expect the transpiler to be able to transpile the fully custom format"
    result[0].clause == "(DAYOFWEEK(:this) = :clause_0_value_dayofweek AND HOUR(:this) = :clause_0_value_hourofday)"
    result[0].parameters == [
      "clause_0_value_dayofweek": 1,
      "clause_0_value_hourofday": 15
    ]

    result[
      1
    ].clause == "(DAYOFWEEK(CONVERT_TZ(:this, 'UTC', '+05:00')) = :clause_0_value_dayofweek AND HOUR(CONVERT_TZ(:this, 'UTC', '+05:00')) = :clause_0_value_hourofday)"
    result[1].parameters == [
      "clause_0_value_dayofweek": 1,
      "clause_0_value_hourofday": 15
    ]
  }


  def "it can transpile comparison of partial dates" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a fully custom format"
    final JPQLQuery result = transpiler.transpile(
      "locale:(en)format:(EEEE HH'h'mm'm')(Tuesday 20h30m):(Monday 15h20m)"
    )

    then: "we expect the transpiler to be able to transpile the fully custom format"
    result.clause == [
      "((",
      "DAYOFWEEK(:this) >= :clause_0_min_dayofweek AND (",
      "DAYOFWEEK(:this) != :clause_0_min_dayofweek OR HOUR(:this) >= :clause_0_min_hourofday AND (",
      "HOUR(:this) != :clause_0_min_hourofday OR MINUTE(:this) >= :clause_0_min_minuteofhour",
      ")",
      ")",
      " AND ",
      "DAYOFWEEK(:this) <= :clause_0_max_dayofweek AND (",
      "DAYOFWEEK(:this) != :clause_0_max_dayofweek OR HOUR(:this) <= :clause_0_max_hourofday AND (",
      "HOUR(:this) != :clause_0_max_hourofday OR MINUTE(:this) <= :clause_0_max_minuteofhour",
      ")",
      ")",
      "))"
    ].join("")

    result.parameters == [
      "clause_0_min_dayofweek": 1,
      "clause_0_min_hourofday": 15,
      "clause_0_min_minuteofhour": 20,
      "clause_0_max_dayofweek": 2,
      "clause_0_max_hourofday": 20,
      "clause_0_max_minuteofhour": 30
    ]
  }

  def "it can rebalance partial filters" () {
    given: "a transpiler"
    final DateTimeJPQLSelectionTranspiler transpiler = new DateTimeJPQLSelectionTranspiler()

    when: "we try to transpile a fully custom format"
    final JPQLQuery[] results = [
      transpiler.transpile("format:(HH'h'X)(08h-03):(15h+05)"),
      transpiler.transpile("format:(HH'h'X)(15h+05):(08h-03)")
    ]

    then: "we expect the transpiler to be able to transpile the fully custom format"
    for (final JPQLQuery result : results) {
      result.clause == "(HOUR(CONVERT_TZ(:this, 'UTC', '+05:00')) >= :clause_0_min_hourofday AND HOUR(CONVERT_TZ(:this, 'UTC', '-03:00')) <= :clause_0_max_hourofday)"
      result.parameters == [
        "clause_0_value_min_hourofday": 15,
        "clause_0_value_max_hourofday": 8
      ]
    }
  }
}
