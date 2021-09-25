package org.geysermc.configutils.loader.validate;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.exception.ImproperConfigValueException;

public class ValidationResult {
  private final ImproperConfigValueException error;

  private final Object value;

  private ValidationResult(ImproperConfigValueException error, Object value) {
    this.error = error;
    this.value = value;
  }

  @NonNull
  public static ValidationResult ok(@NonNull Object value) {
    return new ValidationResult(null, Objects.requireNonNull(value));
  }

  @NonNull
  public static ValidationResult failed(@NonNull String errorMessage) {
    Objects.requireNonNull(errorMessage);
    return new ValidationResult(new ImproperConfigValueException(errorMessage), null);
  }

  public ImproperConfigValueException error() {
    return error;
  }

  public Object value() {
    return value;
  }

  public boolean success() {
    return error == null;
  }
}
