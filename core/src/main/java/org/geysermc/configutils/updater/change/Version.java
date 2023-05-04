package org.geysermc.configutils.updater.change;

import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class Version {
  private final int versionNumber;
  private final Map<String, String> keyRenames;
  private final Map<String, Map<Object, Object>> valueRemoves;

  Version(
      int versionNumber,
      @NonNull Map<String, String> keyRenames,
      @NonNull Map<String, Map<Object, Object>> valueRemoves) {
    this.versionNumber = versionNumber;
    this.keyRenames = Objects.requireNonNull(keyRenames);
    this.valueRemoves = Objects.requireNonNull(valueRemoves);
  }

  public int versionNumber() {
    return versionNumber;
  }

  public String oldKeyName(String key) {
    return keyRenames.getOrDefault(key, key);
  }

  public Object newValue(String key, Object currentValue) {
    Map<Object, Object> removedValues = valueRemoves.get(key);
    if (removedValues != null) {
      return removedValues.getOrDefault(currentValue, currentValue);
    }
    return currentValue;
  }
}
