package org.geysermc.configutils.parser.placeholder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class Placeholders {
  private final Map<String, Supplier<Object>> placeholders = new HashMap<>();

  public boolean addPlaceholder(String name, Supplier<Object> supplier) {
    return placeholders.put(name, supplier) != null;
  }

  public Object placeholder(String name) {
    Supplier<?> placeholder = placeholders.get(name);
    if (placeholder == null) {
      return null;
    }
    return placeholder.get();
  }

  public boolean isPlaceholder(String name) {
    return placeholder(name) != null;
  }

  public boolean removePlaceholder(String name) {
    return placeholders.remove(name) != null;
  }
}
