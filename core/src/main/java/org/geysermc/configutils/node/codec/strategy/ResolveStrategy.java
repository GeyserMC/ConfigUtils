package org.geysermc.configutils.node.codec.strategy;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.context.NodeContext;

public interface ResolveStrategy<T> {
  T resolve(AnnotatedType type, NodeContext context);
}
