package org.geysermc.configutils.updater.change;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class Changes {
  private final List<Version> changes;
  private List<Version> selected;

  private Changes(List<Version> changes) {
    this.changes = changes;
    // we'll sort the versions so that we can loop through them easily
    changes.sort(Comparator.comparingInt(Version::versionNumber));
  }

  public static Builder builder() {
    return new Builder();
  }

  public static VersionBuilder versionBuilder() {
    return new VersionBuilder();
  }

  public boolean select(int currentVersion, int newVersion) {
    boolean wasSelected = selected != null;
    selected = allVersionsBetween(currentVersion, newVersion);
    return wasSelected;
  }

  public String oldKeyName(String newKeyName) {
    if (selected == null) {
      throw new IllegalStateException("Select a version range first");
    }
    String oldKeyName = newKeyName;
    // this works backwards. New name to old name
    for (int i = selected.size() - 1; i >= 0; i--) {
      oldKeyName = selected.get(i).oldKeyName(oldKeyName);
    }
    return oldKeyName;
  }

  public Object newValue(String key, Object currentValue) {
    if (selected == null) {
      throw new IllegalStateException("Select a version range first");
    }
    //todo should probably add some checks so that classes that don't override toString won't end
    // up in the config
    Object newValue = currentValue;
    for (Version version : selected) {
      newValue = version.newValue(key, newValue);
    }
    return newValue;
  }

  @NonNull
  public List<Version> allVersionsBetween(int currentVersion, int newVersion) {
    List<Version> versions = new ArrayList<>();
    for (Version version : changes) {
      if (version.versionNumber() <= currentVersion) {
        continue;
      }
      if (version.versionNumber() > newVersion) {
        break;
      }
      versions.add(version);
    }
    return versions;
  }

  public static final class Builder {
    private final List<Version> changes = new ArrayList<>();

    private Builder() {
    }

    @NonNull
    public Builder version(int version, @NonNull VersionBuilder builder) {
      changes.add(Objects.requireNonNull(builder).build(version));
      return this;
    }

    @NonNull
    public Changes build() {
      return new Changes(changes);
    }
  }
}
