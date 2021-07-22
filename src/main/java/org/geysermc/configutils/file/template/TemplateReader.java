package org.geysermc.configutils.file.template;

import java.io.BufferedReader;
import java.util.List;
import org.geysermc.configutils.util.FileUtils;

@FunctionalInterface
public interface TemplateReader {
  BufferedReader read(String configName);

  default List<String> readLines(String configName) {
    return FileUtils.readBuffered(read(configName));
  }
}
