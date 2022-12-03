package org.geysermc.configutils.node;

import java.lang.reflect.Type;

public abstract class Node {
  private final String key;
  private final Type type;
  private final Object value;

  Node(String key, Type type, Object value) {
    this.key = key;
    this.type = type;
    this.value = value;
  }

  public String key() {
    return key;
  }

  public Type type() {
    return type;
  }

  public Object value() {
    return value;
  }
}
