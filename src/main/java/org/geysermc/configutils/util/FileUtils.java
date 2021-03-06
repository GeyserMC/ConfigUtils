package org.geysermc.configutils.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
  public static List<String> readPath(Path path) {
    try {
      return Files.readAllLines(path);
    } catch (NoSuchFileException e) {
      return null;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static BufferedReader readUrl(URL url) {
    if (url == null) {
      return null;
    }

    try {
      return new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static List<String> readBuffered(BufferedReader reader) {
    try {
      List<String> result = new ArrayList<>();
      for (;;) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        result.add(line);
      }
      return result;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static void writeToPath(Path path, List<String> lines) {
    try {
      Files.createDirectories(path.getParent());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create directories for " + path, e);
    }

    try {
      Files.write(path, lines);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write to path", e);
    }
  }
}
