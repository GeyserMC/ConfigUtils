package org.geysermc.configutils.parser.template.action.storage.predefined;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.parser.template.action.storage.Singleton;
import org.geysermc.configutils.parser.template.action.storage.Storable;

public class UsedConfigsStorage implements Storable, Singleton {
  private final Set<String> usedConfigs = new HashSet<>();

  public UsedConfigsStorage(@Nullable String thisConfigFileName) {
    if (thisConfigFileName != null) {
      usedConfigs.add(thisConfigFileName);
    }
  }

  public void addUsed(@NonNull String configFileName) {
    usedConfigs.add(Objects.requireNonNull(configFileName));
  }

  @NonNull
  public Set<String> usedConfigs() {
    return Collections.unmodifiableSet(usedConfigs);
  }
}
