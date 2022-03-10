package org.geysermc.configutils.file.codec;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.util.FileUtils;

public final class PathFileCodec implements FileCodec {
  private static final PathFileCodec instance = new PathFileCodec(null);

  private final String path;

  private PathFileCodec(@Nullable String path) {
    this.path = path;
  }

  public static PathFileCodec instance() {
    return instance;
  }

  public static PathFileCodec of(String path) {
    return new PathFileCodec(path);
  }

  @Override
  public List<String> read(String configName) {
    return FileUtils.readPath(path(configName));
  }

  @Override
  public boolean write(String configName, List<String> lines) {
    return FileUtils.writeToPath(path(configName), lines);
  }

  private Path path(String configName) {
    if (path != null) {
      return Paths.get(path, configName);
    }
    return Paths.get(configName);
  }
}
