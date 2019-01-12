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

package org.liara.selection.processor;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@FunctionalInterface
public interface ProcessorExecutor<Result>
{
  static <Result> @NonNull ProcessorExecutor<Result> execute (
    @NonNull final Processor<Result> processor
  )
  {
    return (@NonNull final Iterable<@NonNull ProcessorCall> calls) -> {
      @NonNull final List<Result> results = new ArrayList<>();

      for (@NonNull final ProcessorCall call : calls) {
        results.add(call.call(processor));
      }

      return results;
    };
  }

  static <Result> @NonNull ProcessorExecutor<Result> executeIf (
    @NonNull final ProcessorExecutor<Result> processor,
    @NonNull final Function<@NonNull ProcessorCall, @NonNull Boolean> condition
  )
  {
    return (@NonNull final Iterable<@NonNull ProcessorCall> calls) -> {
      @NonNull final List<Result> results = new ArrayList<>();

      for (@NonNull final ProcessorCall call : calls) {
        if (condition.apply(call)) {
          results.addAll(processor.execute(call));
        }
      }

      return results;
    };
  }

  static <Result> @NonNull ProcessorExecutor<Result> field (
    @NonNull final String field, @NonNull final ProcessorExecutor<Result> processor
  )
  {
    return (@NonNull final Iterable<@NonNull ProcessorCall> calls) -> {
      @NonNull final List<Result> results = new ArrayList<>();

      for (@NonNull final ProcessorCall call : calls) {
        if (field.equals(call.getIdentifier(0))) {
          results.addAll(processor.execute(call.next()));
        }
      }

      return results;
    };
  }

  static <Result> @NonNull ProcessorExecutor<Result> fields (
    @NonNull final Map<@NonNull String, @NonNull ProcessorExecutor<Result>> bindings
  )
  {
    @NonNull final Map<@NonNull String, @NonNull ProcessorExecutor<Result>> copy = new HashMap<>(bindings);

    return (@NonNull final Iterable<@NonNull ProcessorCall> calls) -> {
      @NonNull final List<Result> results = new ArrayList<>();

      for (@NonNull final ProcessorCall call : calls) {
        if (copy.containsKey(call.getIdentifier(0))) {
          results.addAll(copy.get(call.getIdentifier(0)).execute(call.next()));
        }
      }

      return results;
    };
  }

  static <Result> @NonNull ProcessorExecutor<Result> all (
    @NonNull final ProcessorExecutor<Result>... processors
  )
  {
    return all(Arrays.asList(processors));
  }

  static <Result> @NonNull ProcessorExecutor<Result> all (
    @NonNull final List<@NonNull ProcessorExecutor<Result>> processors
  )
  {
    @NonNull final List<@NonNull ProcessorExecutor<Result>> copy = new ArrayList<>(processors);

    return (@NonNull final Iterable<@NonNull ProcessorCall> calls) -> {
      @NonNull final List<Result> results = new ArrayList<>();

      for (@NonNull final ProcessorExecutor<Result> executor : copy) {
        results.addAll(executor.execute(calls));
      }

      return results;
    };
  }

  default <Next> @NonNull ProcessorExecutor<Next> map (@NonNull final Function<Result, Next> mapper) {
    return (@NonNull final Iterable<@NonNull ProcessorCall> calls) -> execute(calls).stream().map(mapper).collect(
      Collectors.toList());
  }

  default @NonNull List<@NonNull Result> execute (@NonNull final ProcessorCall... calls) {
    return execute(Arrays.asList(calls));
  }

  @NonNull List<@NonNull Result> execute (@NonNull final Iterable<@NonNull ProcessorCall> calls);
}
