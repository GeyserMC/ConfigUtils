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
import org.geysermc.configutils.node.context.MetaOptions;
import org.geysermc.configutils.node.context.NodeContext;
import org.geysermc.configutils.node.meta.Range;

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
  public Object deserialize(AnnotatedType type, Object rawValue, NodeContext context) {
    if (!(rawValue instanceof Map<?, ?>)) {
      throw new IllegalStateException("An object is serialized from a map");
    }
    Map<?, ?> valueAsMap = (Map<?, ?>) rawValue;

    Map<String, Object> validEntries = new HashMap<>();
    for (Entry<String, MetaOptions> entry : resolveStrategy.resolve(type, context).entrySet()) {
      String key = entry.getKey();
      MetaOptions meta = entry.getValue();

      String rawKey = context.options().nameEncoder().apply(key);
      System.out.println(rawKey);
      Object rawEntryValue = valueAsMap.get(rawKey);

      Object entryValue = null;
      if (rawEntryValue != null) {
        try {
          entryValue = meta.typeCodec().deserialize(meta.type(), rawEntryValue, context);
        } catch (Throwable throwable) {
          if (!meta.defaultOnFailure()) {
            throw throwable;
          }
          //todo add logger and only log on debug
          throwable.printStackTrace();
        }
      }
      if (entryValue == null) {
        entryValue = meta.deserializedDefaultValue();
      }

      if (!meta.isInRange(entryValue)) {
        Range range = meta.range();
        //todo DefaultOnFailure & add error handler option
        throw new IndexOutOfBoundsException(String.format(
            "'%s' (key: %s) is not in the allowed range of from: %s, to: %s!",
            entryValue, rawKey, range.from(), range.to()
        ));
      }

      validEntries.put(key, entryValue);
    }
    return embodimentStrategy.embody(type, validEntries);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public Object serialize(AnnotatedType type, Object value, NodeContext context) {
    Map<String, Object> validEntries = embodimentStrategy.disembody(value);
    Map<String, Object> mappings = new HashMap<>();
    for (Entry<String, MetaOptions> entry : resolveStrategy.resolve(type, context).entrySet()) {
      String key = entry.getKey();
      MetaOptions meta = entry.getValue();

      Object entryValue = validEntries.get(key);
      if (entryValue == null) {
        entryValue = meta.deserializedDefaultValue();
      }

      if (!meta.isInRange(entryValue)) {
        String rawKey = context.options().nameEncoder().apply(key);
        Range range = meta.range();
        //todo DefaultOnFailure & add error handler option
        throw new IndexOutOfBoundsException(String.format(
            "'%s' (key: %s) is not in the allowed range of from: %s, to: %s!",
            entryValue, rawKey, range.from(), range.to()
        ));
      }

      mappings.put(
          context.options().nameEncoder().apply(key),
          ((TypeCodec) meta.typeCodec()).serialize(meta.type(), entryValue, context)
      );
    }
    return mappings;
  }
}
