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

  public String runPlaceholder(String line) {
    // convert: bla bla ${placeholder} to: placeholder
    int placeholderIndex = line.indexOf("${");
    if (placeholderIndex == -1) {
      return line;
    }

    String name = line.substring(placeholderIndex);
    int placeholderCloseIndex = name.indexOf('}');
    if (placeholderCloseIndex == -1) {
      return line;
    }
    name = name.substring(0, placeholderCloseIndex);

    // check if placeholder has been registered
    Object placeholder = getPlaceholder(name);
    if (placeholder == null) {
      return line;
    }

    if (placeholder instanceof Supplier) {
      Object response = ((Supplier<Object>) placeholder).get();
      return line.replaceFirst("\\$\\{" + name + "}", response.toString());
    }
    return placeholder.toString();
  }

  public Object getPlaceholder(String name) {
    return placeholders.get(name);
  }

  public boolean isPlaceholder(String name) {
    return getPlaceholder(name) != null;
  }

  public boolean removePlaceholder(String name) {
    return placeholders.remove(name) != null;
  }
}
