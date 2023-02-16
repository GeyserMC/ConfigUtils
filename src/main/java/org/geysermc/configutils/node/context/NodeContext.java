package org.geysermc.configutils.node.context;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.node.codec.type.TypeCodec;

public final class NodeContext {
  private final RegisteredCodecs registeredCodecs;

  //todo these are global options, add per node options
  // it'd know the type so that it can retrieve related options for you e.g.:
  // is the comment translatable, what's the default value,
  // mark the node (and thus the config) as changed,
  // maybe even conditions? (e.g. enable-global-linking can only be true when linking is enabled)
  private final NodeOptions options;

  public NodeContext(RegisteredCodecs registeredCodecs, NodeOptions options) {
    this.registeredCodecs = registeredCodecs;
    this.options = options;
  }

  public <T> TypeCodec<T> codecFor(AnnotatedType type) {
    return registeredCodecs.get(type);
  }

  public NodeOptions options() {
    return options;
  }

  public MetaOptions createMeta(AnnotatedType type) {
    return new MetaOptions(this, type);
  }
}
