package org.geysermc.configutils.loader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ConfigLoader {
  @NonNull
  public <T> T load(@NonNull Map<String, Object> data, @NonNull Class<T> mapTo) {
    Objects.requireNonNull(data);
    Objects.requireNonNull(mapTo);

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
          Object value = data.get(getCorrectName(field.getName()));
          if (value == null) {
            continue;
          }

          field.setAccessible(true);

          // load subclass
          if (value instanceof Map && !field.getType().isAssignableFrom(Map.class)) {
            value = load((Map<String, Object>) value, field.getType());
          }

          if (field.getType().isEnum()) {
            String upperCase = ((String) value).toUpperCase(Locale.ROOT);
            try {
              value = Enum.valueOf((Class<? extends Enum>) field.getType(), upperCase);
            } catch (IllegalArgumentException ignored) {
              value = Enum.valueOf((Class<? extends Enum>) field.getType(), upperCase.replace('_', '-'));
            }
          }

          try {
            field.set(instance, value);
          } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to set field " + field.getName(), e);
          }
        }
      }
      current = current.getSuperclass();
    }
    return instance;
  }

  private String getCorrectName(String fieldName) {
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
