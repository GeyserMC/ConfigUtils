package org.geysermc.configutils.node.context.meta;

import java.lang.annotation.Annotation;
import java.util.List;

public interface MetaCacheMapper<A extends Annotation, T> {
  T map(List<String> lines);

  Class<A> annotationClass();
}
