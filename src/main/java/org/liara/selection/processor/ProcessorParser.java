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

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.selection.ThrowingErrorListener;
import org.liara.selection.TranspilationException;
import org.liara.selection.antlr.ProcessorsBaseListener;
import org.liara.selection.antlr.ProcessorsLexer;
import org.liara.selection.antlr.ProcessorsParser;

public class ProcessorParser
    extends ProcessorsBaseListener {

  @NonNull
  private final List<ProcessorCall> _result;

  public ProcessorParser() {
    _result = new ArrayList<>();
  }

  @Override
  public void enterProcessors(final ProcessorsParser.@NonNull ProcessorsContext context) {
    _result.clear();
  }

  @Override
  public void exitProcessor(final ProcessorsParser.@NonNull ProcessorContext context) {
    @NonNull final List<Object> configurationParameters = parseParameters(
        context.configurationParameters);
    @NonNull final List<Object> executionParameters = parseParameters(context.executionParameters);

    _result.add(new ProcessorCall(context.identifier().getText(), configurationParameters,
        executionParameters));
  }

  private @NonNull List<@NonNull Object> parseParameters(
      final ProcessorsParser.@Nullable ParametersContext configurationParameters
  ) {
    if (configurationParameters == null) {
      return new ArrayList<>(0);
    }

    @NonNull final List<ProcessorsParser.@NonNull ParameterContext> contexts = configurationParameters
        .parameter();
    @NonNull final List<@NonNull Object> parameters = new ArrayList<>(contexts.size());

    for (final ProcessorsParser.@NonNull ParameterContext context : contexts) {
      parameters.add(parseParameter(context));
    }

    return parameters;
  }

  private @NonNull Object parseParameter(final ProcessorsParser.@NonNull ParameterContext context) {
    if (context.integerParameter() != null) {
      return Long.parseLong(context.integerParameter().getText());
    } else if (context.doubleParameter() != null) {
      return Double.parseDouble(context.doubleParameter().getText());
    } else if (context.stringParameter() != null) {
      @NonNull final String result = context.stringParameter().getText();
      return result.substring(1, result.length() - 1);
    } else {
      return context.constantParameter().getText().toLowerCase();
    }
  }

  public @NonNull ProcessorCall[] transpile(@NonNull final CharSequence expression) {
    @NonNull final ProcessorsLexer lexer = new ProcessorsLexer(
        CharStreams.fromString(expression.toString()));
    @NonNull final ProcessorsParser parser = new ProcessorsParser(new CommonTokenStream(lexer));

    ParseTreeWalker.DEFAULT.walk(this, parser.processors());

    return _result.toArray(new ProcessorCall[0]);
  }

  public @NonNull ProcessorCall[] tryToTranspile(@NonNull final CharSequence expression)
      throws TranspilationException {
    @NonNull final ProcessorsLexer lexer = new ProcessorsLexer(
        CharStreams.fromString(expression.toString()));
    lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
    @NonNull final ProcessorsParser parser = new ProcessorsParser(new CommonTokenStream(lexer));
    parser.addErrorListener(ThrowingErrorListener.INSTANCE);

    ParseTreeWalker.DEFAULT.walk(this, parser.processors());

    return _result.toArray(new ProcessorCall[0]);
  }
}
