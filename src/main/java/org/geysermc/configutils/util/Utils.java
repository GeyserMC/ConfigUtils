package org.geysermc.configutils.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils {
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
}
