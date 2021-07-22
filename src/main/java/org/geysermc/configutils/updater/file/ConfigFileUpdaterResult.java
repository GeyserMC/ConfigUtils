package org.geysermc.configutils.updater.file;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ConfigFileUpdaterResult {
  private final List<String> lines;
  private final Map<String, Object> mappedYaml;
  private final Set<String> notMapped;
  private final List<String> changed;

  private final Throwable error;

  private ConfigFileUpdaterResult(
      List<String> lines,
      Map<String, Object> mappedYaml,
      Set<String> notMapped,
      List<String> changed,
      Throwable error) {
    this.lines = lines;
    this.mappedYaml = mappedYaml;
    this.notMapped = notMapped;
    this.changed = changed;
    this.error = error;
  }

  public static ConfigFileUpdaterResult.Ok ok(
      List<String> lines,
      Map<String, Object> newVersion,
      Set<String> notFound,
      List<String> changed) {
    return new ConfigFileUpdaterResult.Ok(lines, newVersion, notFound, changed);
  }

  public static ConfigFileUpdaterResult.Failed failed(Throwable error) {
    return new ConfigFileUpdaterResult.Failed(error);
  }

  @Nullable
  public List<String> lines() {
    return lines;
  }

  @Nullable
  public Map<String, Object> mappedYaml() {
    return mappedYaml;
  }

  @Nullable
  public Set<String> notMappedLines() {
    return notMapped;
  }

  @Nullable
  public List<String> changedLines() {
    return changed;
  }

  @Nullable
  public Throwable error() {
    return error;
  }

  public boolean succeeded() {
    return error == null;
  }

  @SuppressWarnings("ConstantConditions")
  public static final class Ok extends ConfigFileUpdaterResult {
    private Ok(
        List<String> lines,
        Map<String, Object> newVersion,
        Set<String> notMapped,
        List<String> changed) {
      super(lines, newVersion, notMapped, changed, null);
    }

    @Override
    @NonNull
    public List<String> lines() {
      return super.lines();
    }

    @Override
    @NonNull
    public Set<String> notMappedLines() {
      return super.notMappedLines();
    }

    @Override
    @NonNull
    public List<String> changedLines() {
      return super.changedLines();
    }
  }

  @SuppressWarnings("ConstantConditions")
  public static final class Failed extends ConfigFileUpdaterResult {
    private Failed(Throwable error) {
      super(null, null,null, null, error);
    }

    @Override
    @NonNull
    public Throwable error() {
      return super.error();
    }
  }
}
