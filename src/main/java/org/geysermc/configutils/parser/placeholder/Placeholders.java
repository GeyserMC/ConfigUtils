package org.geysermc.configutils.parser.placeholder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class Placeholders {
  private final Map<String, Object> placeholders = new HashMap<>();

  public boolean addPlaceholder(String name, Supplier<Object> supplier) {
    return placeholders.put(name, supplier) != null;
  }

  public boolean addPlaceholder(String name, Object value) {
    return placeholders.put(name, value) != null;
  }

  public Object placeholder(String name) {
    Object placeholder = placeholders.get(name);
    if (placeholder instanceof Supplier) {
      return ((Supplier<?>) placeholder).get();
    }
    return placeholder;
  }

  public boolean isPlaceholder(String name) {
    return placeholder(name) != null;
  }

  public boolean removePlaceholder(String name) {
    return placeholders.remove(name) != null;
  }
}
