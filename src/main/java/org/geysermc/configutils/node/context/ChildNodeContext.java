package org.geysermc.configutils.node.context;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.node.context.option.NodeOptions;

public class ChildNodeContext extends NodeContext {
  private final NodeContext parent;
  private final String key;

  private boolean changed = false;

  ChildNodeContext(
      @NonNull NodeContext parent,
      @NonNull AnnotatedType type,
      @NonNull String key
  ) {
    super(type);
    this.parent = Objects.requireNonNull(parent);
    this.key = Objects.requireNonNull(key);
    init();
  }

  @Override
  public NodeOptions options() {
    return parent.options();
  }

  @Override
  public RegisteredCodecs codecs() {
    return parent.codecs();
  }

  @Override
  public String fullKey() {
    String fullKey = parent.fullKey();
    if (fullKey.isEmpty()) {
      return key();
    }
    return fullKey + "." + key();
  }

  /**
   * Returns the key of this node, or null if this is the root node.
   */
  @Override
  public @Nullable String key() {
    return key;
  }

  @Override
  public void markChanged() {
    changed = true;
    parent.markChanged();
  }

  @Override
  public boolean changed() {
    return changed;
  }
}
