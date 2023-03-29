package org.geysermc.configutils.file.codec;

public interface FileCodec {
  /**
   * Reads the specified file
   *
   * @param file the file to read
   * @return a list of all the lines in the specified file, or null when the file does not exist
   * @throws IllegalStateException if something unexpected happened while reading the file
   */
  String read(String file) throws IllegalStateException;

  /**
   * Writes the provided data to the specified file
   *
   * @param file  the file to write to
   * @param content the data to write
   * @throws IllegalStateException if something unexpected happened while writing the file
   */
  void write(String file, String content) throws IllegalStateException;
}
