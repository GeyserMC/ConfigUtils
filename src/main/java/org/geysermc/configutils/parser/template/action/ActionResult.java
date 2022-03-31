package org.geysermc.configutils.parser.template.action;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ActionResult {
  private static final ActionResult OK = new ActionResult(null, null);

  private final Throwable error;
  private final List<String> linesToAdd;

  private ActionResult(Throwable error, List<String> linesToAdd) {
    this.error = error;
    this.linesToAdd = linesToAdd;
  }

  @NonNull
  public static ActionResult ok() {
    return OK;
  }

  @NonNull
  public static ActionResult failed(@NonNull Throwable error) {
    return new ActionResult(Objects.requireNonNull(error), null);
  }

  @NonNull
  public static ActionResult failed(@NonNull String errorMessage) {
    return failed(new IllegalStateException(Objects.requireNonNull(errorMessage)));
  }

  @NonNull
  public static ActionResult addLines(@NonNull String... lines) {
    return new ActionResult(null, Arrays.asList(lines));
  }

  @NonNull
  public static ActionResult addLines(@NonNull List<String> lines) {
    return new ActionResult(null, lines);
  }

  @Nullable
  public Throwable error() {
    return error;
  }

  @Nullable
  public List<String> linesToAdd() {
    return linesToAdd;
  }

  public boolean succeeded() {
    return error == null;
  }

  public boolean hasLinesToAdd() {
    return linesToAdd != null;
  }
}
