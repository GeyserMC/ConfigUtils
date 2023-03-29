package org.geysermc.configutils.updater.file;

import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConfigFileUpdaterResult {
  private final Map<Object, Object> config;
  private final Set<String> notMapped;

  private final Throwable error;

  private ConfigFileUpdaterResult(
      Map<Object, Object> config,
      Set<String> notMapped,
      Throwable error
  ) {
    this.config = config;
    this.notMapped = notMapped;
    this.error = error;
  }

  public static ConfigFileUpdaterResult ok(Map<Object, Object> config, Set<String> notMapped) {
    return new ConfigFileUpdaterResult(config, notMapped, null);
  }

  public static ConfigFileUpdaterResult upToDate() {
    return new ConfigFileUpdaterResult(null, null, null);
  }

  public static ConfigFileUpdaterResult failed(Throwable error) {
    return new ConfigFileUpdaterResult(null, null, error);
  }

  @Nullable
  public Map<Object, Object> config() {
    return config;
  }

  @Nullable
  public Set<String> notMapped() {
    return notMapped;
  }

  @Nullable
  public Throwable error() {
    return error;
  }

  public boolean succeeded() {
    return error == null;
  }

  public boolean unchanged() {
    return config == null && succeeded();
  }
}
