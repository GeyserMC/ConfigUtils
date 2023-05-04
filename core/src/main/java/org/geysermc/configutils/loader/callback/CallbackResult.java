package org.geysermc.configutils.loader.callback;

import java.util.Objects;
import java.util.concurrent.Callable;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

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

  @MonotonicNonNull
  public IllegalStateException error() {
    return error;
  }

  public boolean success() {
    return error == null;
  }

  public CallbackResult ifSucceeded(Callable<CallbackResult> onSucceeded) {
    try {
      CallbackResult result = onSucceeded.call();
      return result != null ? result : this;
    } catch (Exception exception) {
      return failed(String.format(
          "An unknown error happened while executing ifSucceeded: %s",
          exception.getMessage()
      ));
    }
  }
}
