package org.geysermc.configutils.updater.change;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class VersionBuilder {
  private final Map<String, String> keyRenames = new HashMap<>();
  private final Map<String, Map<Object, Object>> valueRemoves = new HashMap<>();

  VersionBuilder() {
  }

  @NonNull
  public VersionBuilder keyRenamed(@NonNull String oldName, @NonNull String newName) {
    Objects.requireNonNull(oldName);
    Objects.requireNonNull(newName);

    // has to do with the way we convert configs. We receive all the new names and we'll have to
    // convert the new ones to the old ones to know what value the new names should get.
    keyRenames.put(newName, oldName);
    return this;
  }

  @NonNull
  public VersionBuilder valueChanged(
      @NonNull String key,
      @NonNull Object oldValue,
      @NonNull Object newValue) {
    // changing a value is basically removing the old one and replacing it with a different one
    return valueRemoved(key, oldValue, newValue);
  }

  @NonNull
  public VersionBuilder valueRemoved(
      @NonNull String key,
      @NonNull Object removedValue,
      @NonNull Object fallback) {

    Objects.requireNonNull(key);
    Objects.requireNonNull(removedValue);
    Objects.requireNonNull(fallback);

    Map<Object, Object> removes = valueRemoves.getOrDefault(key, new HashMap<>());
    removes.put(removedValue, fallback);

    // very likely new
    if (removes.size() == 1) {
      valueRemoves.put(key, removes);
    }
    return this;
  }

  @NonNull
  public Version build(int versionNumber) {
    return new Version(versionNumber, keyRenames, valueRemoves);
  }
}
