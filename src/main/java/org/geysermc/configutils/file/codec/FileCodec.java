package org.geysermc.configutils.file.codec;

import java.util.List;

public interface FileCodec {
  /**
   * Reads the specified file
   *
   * @param file the file to read
   * @return a list of all the lines in the specified file, or null when the file does not exist
   * @throws IllegalStateException if something unexpected happened while reading the file
   */
  List<String> read(String file) throws IllegalStateException;

  /**
   * Writes the provided lines to the specified file
   *
   * @param file  the file to write to
   * @param lines the lines to write
   * @throws IllegalStateException if something unexpected happened while writing the file
   */
  void write(String file, List<String> lines) throws IllegalStateException;
}
