package org.geysermc.configutils.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class FileUtils {
  public static String readPath(Path path) {
    try {
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    } catch (NoSuchFileException e) {
      return null;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void writeToPath(Path path, String content) {
    try {
      Files.createDirectories(path.getParent());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create directories for " + path, e);
    }

    try {
      Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write to path", e);
    }
  }
}
