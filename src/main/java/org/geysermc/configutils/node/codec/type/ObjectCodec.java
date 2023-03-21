package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.node.codec.strategy.object.ObjectEmbodimentStrategy;
import org.geysermc.configutils.node.codec.strategy.object.ObjectResolveStrategy;
import org.geysermc.configutils.node.codec.strategy.object.ProxyEmbodimentStrategy;
import org.geysermc.configutils.node.codec.strategy.object.ReflectionResolveStrategy;
import org.geysermc.configutils.node.context.option.MetaOptions;
import org.geysermc.configutils.node.context.NodeContext;

public final class ObjectCodec extends TypeCodec<Object> {
  public static final ObjectCodec REFLECTION_PROXY_INSTANCE =
      new ObjectCodec(new ReflectionResolveStrategy(), new ProxyEmbodimentStrategy());

  private final ObjectResolveStrategy resolveStrategy;
  private final ObjectEmbodimentStrategy embodimentStrategy;

  public ObjectCodec(
      @NonNull ObjectResolveStrategy resolveStrategy,
      @NonNull ObjectEmbodimentStrategy embodimentStrategy
  ) {
    super(Object.class);
    this.resolveStrategy = Objects.requireNonNull(resolveStrategy);
    this.embodimentStrategy = Objects.requireNonNull(embodimentStrategy);
  }

  @Override
  public Object deserialize(AnnotatedType type, Object inputValue, NodeContext pContext) {
    if (!(inputValue instanceof Map<?, ?>)) {
      throw new IllegalStateException("An object is serialized from a map");
    }
    Map<?, ?> valueAsMap = (Map<?, ?>) inputValue;

    Map<String, Object> validEntries = new HashMap<>();
    for (Entry<String, NodeContext> entry : resolveStrategy.resolve(type, pContext).entrySet()) {
      String key = entry.getKey();
      NodeContext context = entry.getValue();
      MetaOptions meta = context.meta();

      String rawKey = context.options().codec().nameEncoder().apply(key);
      Object value = valueAsMap.get(rawKey);

      if (value != null) {
        try {
          value = context.codec().deserialize(context.type(), value, context);
        } catch (Throwable throwable) {
          if (!meta.defaultOnFailure()) {
            throw throwable;
          }
          //todo add logger and only log on debug
          throwable.printStackTrace();
        }
      }
      validEntries.put(key, meta.applyMeta(rawKey, value));
    }
    return embodimentStrategy.embody(type, validEntries);
  }

  @Override
  public Object serialize(AnnotatedType type, Object inputValue, NodeContext pContext) {
    Map<String, Object> validEntries = embodimentStrategy.disembody(inputValue);
    Map<String, Object> mappings = new HashMap<>();
    for (Entry<String, NodeContext> entry : resolveStrategy.resolve(type, pContext).entrySet()) {
      String key = entry.getKey();
      NodeContext context = entry.getValue();

      String rawKey = context.options().codec().nameEncoder().apply(key);
      Object value = context.meta().applyMeta(rawKey, validEntries.get(key));

      mappings.put(
          context.options().codec().nameEncoder().apply(key),
          context.codec().serialize(context.type(), value, context)
      );
    }
    return mappings;
  }
}
