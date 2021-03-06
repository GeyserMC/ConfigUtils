package org.geysermc.configutils.loader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.exception.ImproperConfigValueException;
import org.geysermc.configutils.loader.callback.CallbackResult;
import org.geysermc.configutils.loader.callback.GenericPostInitializeCallback;
import org.geysermc.configutils.loader.callback.PostInitializeCallback;
import org.geysermc.configutils.loader.validate.ValidationResult;
import org.geysermc.configutils.loader.validate.Validations;

public class ConfigLoader {
  @NonNull
  public <T> T load(
      @NonNull Map<String, Object> data,
      @NonNull Class<T> mapTo,
      @NonNull Validations validations,
      @Nullable Object postInitializeCallbackArgument) {
    return load("", data, mapTo, validations, postInitializeCallbackArgument);
  }

  @NonNull
  protected <T> T load(
      @NonNull String keyPath,
      @NonNull Map<String, Object> data,
      @NonNull Class<T> mapTo,
      @NonNull Validations validations,
      @Nullable Object callbackArgument) {

    Objects.requireNonNull(data);
    Objects.requireNonNull(mapTo);
    Objects.requireNonNull(validations);

    T instance;
    try {
      instance = mapTo.getDeclaredConstructor().newInstance();
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("The class to map to should have a no-arg constructor", e);
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new IllegalStateException("Failed to initialize class to map to", e);
    }

    Class<?> current = mapTo;
    while (!Object.class.equals(current)) {
      for (Field field : current.getDeclaredFields()) {
        int modifiers = field.getModifiers();
        if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {

          String key = correctName(field.getName());
          String fullKey = keyPath + key;

          Object value = data.get(key);
          if (value == null) {
            continue;
          }

          field.setAccessible(true);

          // load subclass
          if (value instanceof Map && !field.getType().isAssignableFrom(Map.class)) {
            value = load(
                fullKey + '.', (Map<String, Object>) value, field.getType(),
                validations, callbackArgument
            );
          }

          if (field.getType().isEnum()) {
            String upperCase = ((String) value).toUpperCase(Locale.ROOT);
            Class<? extends Enum> type = (Class<? extends Enum<?>>) field.getType();
            try {
              value = Enum.valueOf(type, upperCase);
            } catch (IllegalArgumentException ignored) {
              value = Enum.valueOf(type, upperCase.replace('_', '-'));
            }
          }

          if (field.getType() == UUID.class) {
            value = UUID.fromString((String) value);
          }

          try {
            ValidationResult result = validations.validate(fullKey, value);
            if (result.error() != null) {
              result.error().messagePrefix(String.format(
                  "Config option %s does not meet the criteria", fullKey
              ));
              throw result.error();
            }
            value = result.value();
          } catch (ImproperConfigValueException exception) {
            throw exception;
          } catch (Exception exception) {
            throw new IllegalStateException(String.format(
                "An unknown error happened while validating %s",
                fullKey
            ), exception);
          }

          try {
            field.set(instance, value);
          } catch (IllegalArgumentException exception) {
            throw new ImproperConfigValueException(String.format(
                "Failed to set %s (%s) to an instance of %s",
                fullKey, field.getType().getSimpleName(), value.getClass().getSimpleName()
            ));
          } catch (IllegalAccessException exception) {
            throw new IllegalStateException(String.format(
                "Failed to set field %s",
                field.getName()
            ), exception);
          }
        }
      }
      current = current.getSuperclass();
    }

    CallbackResult result = null;

    try_block:
    try {
      if (instance instanceof GenericPostInitializeCallback) {
        //noinspection unchecked,rawtypes
        result = ((GenericPostInitializeCallback) instance).postInitialize(callbackArgument);
        if (!result.success()) {
          // don't throw the exception in the try/catch block
          break try_block;
        }
      }

      if (instance instanceof PostInitializeCallback) {
        result = ((PostInitializeCallback) instance).postInitialize();
      }
    } catch (Exception exception) {
      throw new IllegalStateException(
          "An unknown error happened while executing a post-initialize callback", exception
      );
    }

    if (result != null && !result.success()) {
      throw result.error();
    }

    return instance;
  }

  private String correctName(String fieldName) {
    // convert sendFloodgateData to send-floodgate-data,
    // which is the style of writing config fields
    StringBuilder propertyBuilder = new StringBuilder();
    for (int i = 0; i < fieldName.length(); i++) {
      char current = fieldName.charAt(i);
      if (Character.isUpperCase(current)) {
        propertyBuilder.append('-').append(Character.toLowerCase(current));
      } else {
        propertyBuilder.append(current);
      }
    }
    return propertyBuilder.toString();
  }
}
