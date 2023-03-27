package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.exception.ImproperConfigValueException;
import org.geysermc.configutils.loader.validate.ValidationResult;
import org.geysermc.configutils.node.codec.strategy.object.ObjectEmbodimentStrategy;
import org.geysermc.configutils.node.codec.strategy.object.ObjectResolveStrategy;
import org.geysermc.configutils.node.codec.strategy.object.ProxyEmbodimentStrategy;
import org.geysermc.configutils.node.codec.strategy.object.ReflectionResolveStrategy;
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
    for (NodeContext node : resolveStrategy.resolve(type, pContext)) {
      String rawKey = node.options().codec().nameEncoder().apply(node.key());
      Object value = valueAsMap.get(rawKey);

      // we have to help an empty config section out
      if (node.meta().isSection() && value == null) {
        value = new HashMap<>();
      }

      if (value != null) {
        try {
          value = node.codec().deserialize(node.type(), value, node);
        } catch (Throwable throwable) {
          if (!node.meta().defaultOnFailure()) {
            throw throwable;
          }
          //todo add logger and only log on debug
          throwable.printStackTrace();
        }
      }

      value = node.meta().applyMeta(value);

      try {
        ValidationResult result = node.options().validations().validate(node.fullKey(), value);
        if (result.error() != null) {
          result.error().messagePrefix(String.format(
              "Config node %s does not meet the criteria", node.fullKey()
          ));
          throw result.error();
        }
        value = result.value();
      } catch (ImproperConfigValueException exception) {
        throw exception;
      } catch (Exception exception) {
        throw new IllegalStateException(
            "An unknown error happened while validating " + node.fullKey(),
            exception
        );
      }

      validEntries.put(node.key(), value);
    }
    return embodimentStrategy.embody(type, validEntries);
  }

  @Override
  public Object serialize(AnnotatedType type, Object inputValue, NodeContext pContext) {
    Map<String, Object> validEntries = embodimentStrategy.disembody(inputValue);
    Map<String, Object> mappings = new LinkedHashMap<>();
    for (NodeContext node : resolveStrategy.resolve(type, pContext)) {
      Object value = node.meta().applyMeta(validEntries.get(node.key()));
      mappings.put(
          node.options().codec().nameEncoder().apply(node.key()),
          node.codec().serialize(node.type(), value, node)
      );
    }
    return mappings;
  }

  public ObjectResolveStrategy resolveStrategy() {
    return resolveStrategy;
  }

  public ObjectEmbodimentStrategy embodimentStrategy() {
    return embodimentStrategy;
  }
}
