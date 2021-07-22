package org.geysermc.configutils.updater.renames;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Renames {
  private final Map<Integer, Map<String, String>> renames;

  private Renames(Int2ObjectMap<Map<String, String>> renames) {
    this.renames = renames;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static VersionBuilder versionBuilder() {
    return new VersionBuilder();
  }

  @Nullable
  public Map<String, String> get(int version) {
    return renames.get(version);
  }

  public static final class Builder {
    private final Int2ObjectMap<Map<String, String>> renames = new Int2ObjectOpenHashMap<>();

    private Builder() {}

    @NonNull
    public Builder version(int version, @NonNull VersionBuilder builder) {
      renames.put(version, Objects.requireNonNull(builder).build());
      return this;
    }

    @NonNull
    public Renames build() {
      return new Renames(renames);
    }
  }

  public static final class VersionBuilder {
    private final Map<String, String> renames = new HashMap<>();

    private VersionBuilder() {}

    @NonNull
    public VersionBuilder rename(@NonNull String oldName, @NonNull String newName) {
      Objects.requireNonNull(oldName);
      Objects.requireNonNull(newName);

      // has to do with the way we convert configs. We receive all the new names and we'll have to
      // convert the new ones to the old ones to know what value the new names should get.
      renames.put(newName, oldName);
      return this;
    }

    @NonNull
    public Map<String, String> build() {
      return Collections.unmodifiableMap(renames);
    }
  }
}
