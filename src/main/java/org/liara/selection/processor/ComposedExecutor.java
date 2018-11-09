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

package org.liara.selection.processor;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComposedExecutor<Result>
  implements ProcessorExecutor<Result>
{
  @NonNull
  private final ProcessorExecutor<Result>[] _processors;

  public ComposedExecutor (
    @NonNull final ProcessorExecutor<Result>... processors
  )
  {
    _processors = Arrays.copyOf(processors, processors.length);
  }

  @Override
  public @NonNull Result[] execute (@NonNull final Iterable<@NonNull ProcessorCall> calls) {
    @NonNull final List<Result> results = new ArrayList<>();

    for (@NonNull final ProcessorCall call : calls) {
      for (@NonNull final ProcessorExecutor<Result> executor : _processors) {
        results.addAll(Arrays.asList(executor.execute(Arrays.asList(call))));
      }
    }

    return (Result[]) results.toArray();
  }
}
