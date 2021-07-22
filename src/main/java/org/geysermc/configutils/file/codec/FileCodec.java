package org.geysermc.configutils.file.codec;

import java.util.List;

public interface FileCodec {
  default Object readWrite(String configName, List<String> lines) {
    if (lines == null) {
      return read(configName);
    }
    return write(configName, lines);
  }

  default List<String> read(String configName) {
    return (List<String>) readWrite(configName, null);
  }

  default boolean write(String configName, List<String> lines) {
    return (boolean) readWrite(configName, lines);
  }

  @FunctionalInterface
  interface ReadWriteFileCodec extends FileCodec {
    @Override
    Object readWrite(String configName, List<String> lines);
  }
}
