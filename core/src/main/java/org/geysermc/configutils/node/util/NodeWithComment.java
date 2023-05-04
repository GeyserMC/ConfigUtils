package org.geysermc.configutils.node.util;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NodeWithComment {
  private final Object value;
  private final String comment;

  public NodeWithComment(@NonNull Object value, @NonNull String comment) {
    this.value = Objects.requireNonNull(value);
    this.comment = Objects.requireNonNull(comment);
  }

  public Object value() {
    return value;
  }

  public String comment() {
    return comment;
  }
}
