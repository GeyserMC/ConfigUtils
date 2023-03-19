package org.geysermc.configutils.node.codec.strategy.object;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.geysermc.configutils.node.codec.type.TypeCodec;
import org.geysermc.configutils.node.context.MetaOptions;
import org.geysermc.configutils.node.context.NodeContext;

public final class ReflectionResolveStrategy implements ObjectResolveStrategy {
  @Override
  public Map<String, MetaOptions> resolve(AnnotatedType type, NodeContext context) {
    Class<?> clazz = GenericTypeReflector.erase(type.getType());
    Map<String, MetaOptions> mappings = new HashMap<>();
    for (Method method : clazz.getMethods()) {
      if (method.getParameterCount() != 0) {
        continue;
      }

      // keep the annotations present on the method itself and
      // get the annotations of the return type
      AnnotatedType returnType = GenericTypeReflector.getReturnType(method, type);
      returnType = GenericTypeReflector.updateAnnotations(returnType, method.getAnnotations());

      TypeCodec<?> codec = context.codecFor(returnType);
      if (codec == null) {
        throw new IllegalStateException("No codec registered for type " + returnType);
      }

      mappings.put(method.getName(), context.createMeta(returnType));
    }
    return Collections.unmodifiableMap(mappings);
  }
}
