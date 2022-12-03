package org.geysermc.configutils.node;

import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.node.option.NodeOptions;

public class NodeContext {
  private final RegisteredCodecs registeredCodecs;

  //todo these are global options, add per node options
  // it'd know the type so that it can retrieve related options for you e.g.:
  // is the comment translatable, what's the default value,
  // mark the node (and thus the config) as changed,
  // maybe even conditions? (e.g. enable-global-linking can only be true when linking is enabled)
  private final NodeOptions nodeOptions;

  public NodeContext(RegisteredCodecs registeredCodecs, NodeOptions nodeOptions) {
    this.registeredCodecs = registeredCodecs;
    this.nodeOptions = nodeOptions;
  }
}
