package org.geysermc.configutils.node.codec.strategy;

import java.lang.reflect.AnnotatedType;

public interface EmbodimentStrategy<T, I> {
  T embody(AnnotatedType type, I input);
  I disembody(T embodiment);
}
