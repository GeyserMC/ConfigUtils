package org.geysermc.configutils.file.template;

import java.io.BufferedReader;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.util.FileUtils;

public final class ResourceTemplateReader implements TemplateReader {
  private final ClassLoader classLoader;

  private ResourceTemplateReader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  public static ResourceTemplateReader of(@NonNull ClassLoader classLoader) {
    Objects.requireNonNull(classLoader);
    return new ResourceTemplateReader(classLoader);
  }

  public static ResourceTemplateReader of(@NonNull Class<?> clazz) {
    Objects.requireNonNull(clazz);
    return of(clazz.getClassLoader());
  }

  @Override
  public BufferedReader read(String configName) {
    return FileUtils.readUrl(classLoader.getResource(configName));
  }
}
