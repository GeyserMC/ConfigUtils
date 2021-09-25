package org.geysermc.configutils.loader.validate;

@FunctionalInterface
public interface Validator {
  ValidationResult validate(String key, Object value);
}
