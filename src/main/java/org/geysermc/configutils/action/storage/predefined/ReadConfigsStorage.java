package org.geysermc.configutils.action.storage.predefined;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.action.storage.Singleton;
import org.geysermc.configutils.action.storage.Storable;

public class ReadConfigsStorage implements Storable, Singleton {
  private final Set<String> readConfigs = new HashSet<>();

  public ReadConfigsStorage(@Nullable String thisConfigFileName) {
    if (thisConfigFileName != null) {
      readConfigs.add(thisConfigFileName);
    }
  }

  public void addRead(@NonNull String configFileName) {
    readConfigs.add(Objects.requireNonNull(configFileName));
  }

  @NonNull
  public Set<String> getReadConfigs() {
    return Collections.unmodifiableSet(readConfigs);
  }
}
