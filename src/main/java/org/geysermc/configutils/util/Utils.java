package org.geysermc.configutils.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Utils {
  public static List<String> merge(String[] left, String... right) {
    if (left == null || right == null) {
      return Arrays.asList(left != null ? left : right);
    }

    List<String> list = new ArrayList<>();
    Collections.addAll(list, left);
    Collections.addAll(list, right);
    return list;
  }

  public static String repeat(char toRepeat, int amount) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < amount; i++) {
      builder.append(toRepeat);
    }
    return builder.toString();
  }

  /**
   * Converts sendFloodgateData to send-floodgate-data
   */
  public static String camelCaseToKebabCase(String fieldName) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < fieldName.length(); i++) {
      char current = fieldName.charAt(i);
      if (Character.isUpperCase(current)) {
        builder.append('-').append(Character.toLowerCase(current));
      } else {
        builder.append(current);
      }
    }
    return builder.toString();
  }

  /**
   * Converts ADDED_TO_QUEUE to added-to-queue
   */
  public static String constantCaseToKebabCase(String fieldName) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < fieldName.length(); i++) {
      char current = fieldName.charAt(i);
      if (current == '_') {
        builder.append('-');
      } else {
        builder.append(Character.toLowerCase(current));
      }
    }
    return builder.toString();
  }

  /**
   * Converts added-to-queue to ADDED_TO_QUEUE
   */
  public static String kebabCaseToConstantCase(String fieldName) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < fieldName.length(); i++) {
      char current = fieldName.charAt(i);
      if (current == '-') {
        builder.append('_');
      } else {
        builder.append(Character.toUpperCase(current));
      }
    }
    return builder.toString();
  }
}
