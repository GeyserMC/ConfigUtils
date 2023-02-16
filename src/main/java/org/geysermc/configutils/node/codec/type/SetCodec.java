package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.HashSet;
import java.util.Set;
import org.geysermc.configutils.node.context.NodeContext;

public final class SetCodec extends TypeCodec<Set<?>> {
  public static final SetCodec INSTANCE = new SetCodec();
  public static final TypeToken<Set<?>> TYPE = new TypeToken<Set<?>>() {};

  private SetCodec() {
    super(TYPE);
  }

  @Override
  public Set<?> deserialize(AnnotatedType type, Object toDeserialize, NodeContext context) {
    if (!(toDeserialize instanceof Set<?>)) {
      throw new IllegalStateException("Value should be an instance of Set");
    }
    Set<?> valueAsSet = (Set<?>) toDeserialize;

    if (!(type instanceof AnnotatedParameterizedType)) {
      throw new IllegalStateException("Cannot deserialize a raw Set");
    }
    AnnotatedType[] typeArgs = ((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments();
    if (typeArgs.length != 1) {
      throw new IllegalStateException("A Set should have one type argument");
    }

    AnnotatedType valueType = typeArgs[0];
    TypeCodec<?> valueCodec = context.codecFor(valueType);
    if (valueCodec == null) {
      throw new IllegalStateException("No codec registered for type " + valueType);
    }

    Set<Object> result = new HashSet<>();
    for (Object entry : valueAsSet) {
      result.add(valueCodec.deserialize(valueType, entry, context));
    }
    return result;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Object serialize(AnnotatedType type, Set<?> valueAsSet, NodeContext context) {
    if (!(type instanceof AnnotatedParameterizedType)) {
      throw new IllegalStateException("Cannot serialize a raw Set");
    }
    AnnotatedType[] typeArgs = ((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments();
    if (typeArgs.length != 1) {
      throw new IllegalStateException("A Set should have one type argument");
    }

    AnnotatedType valueType = typeArgs[0];
    TypeCodec valueCodec = context.codecFor(valueType);
    if (valueCodec == null) {
      throw new IllegalStateException("No codec registered for type " + valueType);
    }

    Set<Object> result = new HashSet<>();
    for (Object entry : valueAsSet) {
      result.add(valueCodec.serialize(valueType, entry, context));
    }
    return result;
  }
}
