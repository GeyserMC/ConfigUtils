package org.geysermc.configutils.parser;

import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.action.storage.Storables;

public class TemplateParseResult {
  private final List<String> lines;
  private final Storables storables;
  private final Throwable error;

  private TemplateParseResult(List<String> lines, Storables storables, Throwable error) {
    this.lines = lines;
    this.storables = storables;
    this.error = error;
  }

  @NonNull
  public static TemplateParseResult ok(@NonNull List<String> lines, @NonNull Storables storables) {
    return new TemplateParseResult(
        Objects.requireNonNull(lines),
        Objects.requireNonNull(storables),
        null
    );
  }

  @NonNull
  public static TemplateParseResult failed(@NonNull Throwable error) {
    return new TemplateParseResult(null, null, error);
  }

  @Nullable
  public Throwable error() {
    return error;
  }

  @Nullable
  public List<String> templateLines() {
    return lines;
  }

  @Nullable
  public Storables storables() {
    return storables;
  }

  public boolean succeeded() {
    return error == null;
  }
}
