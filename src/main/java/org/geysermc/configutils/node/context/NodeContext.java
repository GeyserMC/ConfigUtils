package org.geysermc.configutils.node.context;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.node.codec.type.TypeCodec;
import org.geysermc.configutils.node.context.option.MetaOptions;
import org.geysermc.configutils.node.context.option.NodeOptions;

public abstract class NodeContext {
  private final AnnotatedType type;
  private final MetaOptions meta;
  private TypeCodec<?> codec;

  private boolean changed;

  protected NodeContext(@NonNull AnnotatedType type) {
    this.type = Objects.requireNonNull(type);
    this.meta = new MetaOptions(this);
  }

  protected void init() {
    codec = codecFor(type());
    if (codec == null) {
      throw new IllegalStateException("No codec registered for type " + type());
    }
  }

  public AnnotatedType type() {
    return type;
  }

  public MetaOptions meta() {
    return meta;
  }

  public <T> TypeCodec<T> codec() {
    //noinspection unchecked
    return (TypeCodec<T>) codec;
  }

  public abstract RegisteredCodecs codecs();

  public <T> TypeCodec<T> codecFor(AnnotatedType type) {
    return codecs().get(type);
  }

  public abstract NodeOptions options();

  public NodeContext createChildContext(AnnotatedType type, String key) {
    return new ChildNodeContext(this, type, key);
  }

  /**
   * Returns the full key of the node. This combines the parent full key (if it has a parent) and
   * its own key. In contrast to {@link #key()}, the root node will return an empty string instead
   * of null.
   */
  public abstract String fullKey();

  /**
   * Returns the key of this node, or null if this is the root node.
   */
  public abstract @Nullable String key();

  public void markChanged() {
    changed = true;
  }

  public boolean changed() {
    return changed;
  }
}
