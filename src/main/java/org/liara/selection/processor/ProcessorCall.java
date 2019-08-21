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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ProcessorCall {

  @NonNull
  private final List<@NonNull Object> _configurationParameters;

  @NonNull
  private final List<@NonNull Object> _executionParameters;

  @NonNull
  private final String[] _identifier;

  public ProcessorCall(
      @NonNull final String identifier,
      @NonNull final List<@NonNull Object> configurationParameters,
      @NonNull final List<@NonNull Object> executionParameters
  ) {
    _identifier = identifier.split("\\.");
    _configurationParameters = configurationParameters;
    _executionParameters = executionParameters;
  }

  public ProcessorCall(
      @NonNull final String[] identifier,
      @NonNull final List<@NonNull Object> configurationParameters,
      @NonNull final List<@NonNull Object> executionParameters
  ) {
    _identifier = Arrays.copyOf(identifier, identifier.length);
    _configurationParameters = configurationParameters;
    _executionParameters = executionParameters;
  }

  public ProcessorCall(
      @NonNull final String[] identifier, @NonNull final ProcessorCall toCopy
  ) {
    _identifier = Arrays.copyOf(identifier, identifier.length);
    _configurationParameters = toCopy.getConfigurationParameters();
    _executionParameters = toCopy.getExecutionParameters();
  }

  public <Result> @NonNull Result call(@NonNull final Processor<Result> processor) {
    processor.configure(_configurationParameters);
    return processor.execute(_executionParameters);
  }

  public @NonNull String[] getIdentifier() {
    return Arrays.copyOf(_identifier, _identifier.length);
  }

  public @NonNull String getFullIdentifier() {
    return String.join(".", _identifier);
  }

  public @NonNull String getIdentifier(@NonNegative final int index) {
    return _identifier[index];
  }

  public @NonNull ProcessorCall next() {
    return new ProcessorCall(Arrays.copyOfRange(_identifier, 1, _identifier.length), this);
  }

  public @NonNull List<@NonNull Object> getConfigurationParameters() {
    return Collections.unmodifiableList(_configurationParameters);
  }

  public @NonNull List<@NonNull Object> getExecutionParameters() {
    return Collections.unmodifiableList(_executionParameters);
  }
}
