package org.geysermc.configutils.loader.validate;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class Validations {
  private final Map<String, Set<Validator>> validations;

  private Validations(Map<String, Set<Validator>> validations) {
    this.validations = validations;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ValidationResult validate(String key, Object value) {
    Set<Validator> validators = validations.get(key);
    if (validators == null || validators.isEmpty()) {
      return ValidationResult.ok(value);
    }

    ValidationResult result = null; // will never be null, but IntelliJ doesn't like it otherwise
    for (Validator validator : validators) {
      result = validator.validate(key, value);
      if (!result.success()) {
        break;
      }
      value = result.value();
    }
    return result;
  }

  public static final class Builder {
    private final Map<String, Set<Validator>> validations = new HashMap<>();

    private Builder() {
    }

    @NonNull
    public Builder validation(@NonNull String key, @NonNull Validator validator) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(validator);

      validations.computeIfAbsent(key, k -> new LinkedHashSet<>())
          .add(validator);
      return this;
    }

    @NonNull
    public Builder validation(
        @NonNull String key,
        @NonNull Function<Object, ValidationResult> validator) {
      return validation(key, (ignored, value) -> validator.apply(value));
    }

    public Validations build() {
      return new Validations(validations);
    }
  }
}
