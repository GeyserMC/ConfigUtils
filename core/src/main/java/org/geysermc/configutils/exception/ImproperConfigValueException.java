package org.geysermc.configutils.exception;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ImproperConfigValueException extends RuntimeException {
  private String prefix;

  public ImproperConfigValueException(String message) {
    super(message);
  }

  public void messagePrefix(@NonNull String messagePrefix) {
    prefix = Objects.requireNonNull(messagePrefix);
  }

  @Override
  public String getMessage() {
    if (prefix != null) {
      return prefix + ": " + super.getMessage();
    }
    return super.getMessage();
  }
}
