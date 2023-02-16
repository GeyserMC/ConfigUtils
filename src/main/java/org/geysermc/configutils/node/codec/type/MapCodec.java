package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.geysermc.configutils.node.context.NodeContext;

public final class MapCodec extends TypeCodec<Map<?, ?>> {
  public static final MapCodec INSTANCE = new MapCodec();
  public static final TypeToken<Map<?, ?>> TYPE = new TypeToken<Map<?, ?>>() {};

  private MapCodec() {
    super(TYPE);
  }

  @Override
  public Map<?, ?> deserialize(AnnotatedType type, Object toDeserialize, NodeContext context) {
    if (!(toDeserialize instanceof Map<?,?>)) {
      throw new IllegalStateException("Value should be an instance of Map");
    }
    Map<?, ?> valueAsMap = (Map<?, ?>) toDeserialize;

    if (!(type instanceof AnnotatedParameterizedType)) {
      throw new IllegalStateException("Cannot deserialize a raw Map");
    }
    AnnotatedType[] typeArgs = ((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments();
    if (typeArgs.length != 2) {
      throw new IllegalStateException("A Map should have two type arguments");
    }
    AnnotatedType keyType = typeArgs[0];
    AnnotatedType valueType = typeArgs[1];

    TypeCodec<?> keyCodec = context.codecFor(keyType);
    TypeCodec<?> valueCodec = context.codecFor(valueType);

    if (keyCodec == null) {
      throw new IllegalStateException("No codec registered for type " + keyType);
    }
    if (valueCodec == null) {
      throw new IllegalStateException("No codec registered for type " + valueType);
    }

    Map<Object, Object> result = new HashMap<>(valueAsMap.size());
    for (Entry<?, ?> entry : valueAsMap.entrySet()) {
      Object key = keyCodec.deserialize(keyType, entry.getKey(), context);
      Object value = valueCodec.deserialize(valueType, entry.getValue(), context);
      result.put(key, value);
    }
    return result;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Object serialize(AnnotatedType type, Map<?, ?> valueAsMap, NodeContext context) {
    if (!(type instanceof AnnotatedParameterizedType)) {
      throw new IllegalStateException("Cannot serialize raw collections");
    }
    AnnotatedType[] typeArgs = ((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments();
    if (typeArgs.length != 2) {
      throw new IllegalStateException("A Map should have two type arguments");
    }
    AnnotatedType keyType = typeArgs[0];
    AnnotatedType valueType = typeArgs[1];

    TypeCodec keyCodec = context.codecFor(keyType);
    TypeCodec valueCodec = context.codecFor(valueType);

    if (keyCodec == null) {
      throw new IllegalStateException("No codec registered for type " + keyType);
    }
    if (valueCodec == null) {
      throw new IllegalStateException("No codec registered for type " + valueType);
    }

    Map<Object, Object> result = new HashMap<>(valueAsMap.size());
    for (Entry<?, ?> entry : valueAsMap.entrySet()) {
      Object key = keyCodec.serialize(keyType, entry.getKey(), context);
      Object value = valueCodec.serialize(valueType, entry.getValue(), context);
      result.put(key, value);
    }
    return result;
  }
}
