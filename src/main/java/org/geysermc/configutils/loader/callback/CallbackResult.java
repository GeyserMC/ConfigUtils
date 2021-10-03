package org.geysermc.configutils.loader.callback;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CallbackResult {
  private final IllegalStateException error;

  private CallbackResult(IllegalStateException error) {
    this.error = error;
  }

  @NonNull
  public static CallbackResult ok() {
    return new CallbackResult(null);
  }

  @NonNull
  public static CallbackResult failed(@NonNull String errorMessage) {
    Objects.requireNonNull(errorMessage);
    return new CallbackResult(new IllegalStateException(errorMessage));
  }

  @Nullable
  public IllegalStateException error() {
    return error;
  }

  public boolean success() {
    return error == null;
  }
}
