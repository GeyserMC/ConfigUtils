package org.geysermc.configutils.file.codec;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.util.FileUtils;

public final class PathFileCodec implements FileCodec {
  private static final PathFileCodec DEFAULT_INSTANCE = new PathFileCodec(null);

  private final Path path;

  private PathFileCodec(@Nullable Path path) {
    this.path = path;
  }

  public static PathFileCodec instance() {
    return DEFAULT_INSTANCE;
  }

  public static PathFileCodec of(Path path) {
    return new PathFileCodec(path);
  }

  public static PathFileCodec of(String path) {
    if (path != null) {
      return of(Paths.get(path));
    }
    return instance();
  }

  @Override
  public String read(String file) {
    return FileUtils.readPath(path(file));
  }

  @Override
  public void write(String file, String content) {
    FileUtils.writeToPath(path(file), content);
  }

  private Path path(String configName) {
    if (path != null) {
      return path.resolve(configName);
    }
    return Paths.get(configName);
  }
}
