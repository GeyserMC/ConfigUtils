package org.geysermc.configutils.node.codec.strategy.object;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.geysermc.configutils.node.context.NodeContext;
import org.geysermc.configutils.node.context.meta.ConfigSectionMapper;
import org.geysermc.configutils.util.ReflectionUtils;

public final class ReflectionResolveStrategy implements ObjectResolveStrategy {
  @Override
  public List<NodeContext> resolve(AnnotatedType type, NodeContext context) {
    Class<?> clazz = GenericTypeReflector.erase(type.getType());
    List<String> mappingOrder =
        context
            .options()
            .metaCache()
            .cacheIfAbsent(ConfigSectionMapper.MAPPER)
            .get(clazz.getCanonicalName());

    List<NodeContext> mappings = new ArrayList<>();
    for (Method method : clazz.getMethods()) {
      if (method.getParameterCount() != 0 || method.isDefault()) {
        continue;
      }

      // keep the annotations present on the method itself and
      // get the annotations of the return type
      AnnotatedType returnType = GenericTypeReflector.getReturnType(method, type);
      returnType = GenericTypeReflector.updateAnnotations(returnType, method.getAnnotations());

      // add inherited annotations
      Annotation[] inherited = ReflectionUtils.findInheritedAnnotations(method);
      if (inherited.length > 0) {
        returnType = GenericTypeReflector.updateAnnotations(returnType, inherited);
      }

      NodeContext childContext = context.createChildContext(returnType, method.getName());
      if (childContext.meta().isExcluded()) {
        continue;
      }

      mappings.add(childContext);
    }

    if (mappingOrder != null && !mappingOrder.isEmpty()) {
      mappings.sort(Comparator.comparingInt(item -> mappingOrder.indexOf(item.key())));
    }

    return Collections.unmodifiableList(mappings);
  }
}
