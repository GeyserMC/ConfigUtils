package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.List;
import org.geysermc.configutils.node.context.NodeContext;

public final class ListCodec extends TypeCodec<List<?>> {
  public static final ListCodec INSTANCE = new ListCodec();
  public static final TypeToken<List<?>> TYPE = new TypeToken<List<?>>() {};

  private ListCodec() {
    super(TYPE);
  }

  @Override
  public List<?> deserialize(AnnotatedType type, Object toDeserialize, NodeContext context) {
    if (!(toDeserialize instanceof List<?>)) {
      throw new IllegalStateException("Value should be an instance of List");
    }
    List<?> valueAsList = (List<?>) toDeserialize;

    if (!(type instanceof AnnotatedParameterizedType)) {
      throw new IllegalStateException("Cannot deserialize a raw List");
    }
    AnnotatedType[] typeArgs = ((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments();
    if (typeArgs.length != 1) {
      throw new IllegalStateException("A List should have one type argument");
    }

    AnnotatedType valueType = typeArgs[0];
    TypeCodec<?> valueCodec = context.codecFor(valueType);
    if (valueCodec == null) {
      throw new IllegalStateException("No codec registered for type " + valueType);
    }

    List<Object> result = new ArrayList<>();
    for (Object entry : valueAsList) {
      result.add(valueCodec.deserialize(valueType, entry, context));
    }
    return result;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Object serialize(AnnotatedType type, List<?> valueAsList, NodeContext context) {
    if (!(type instanceof AnnotatedParameterizedType)) {
      throw new IllegalStateException("Cannot serialize a raw List");
    }
    AnnotatedType[] typeArgs = ((AnnotatedParameterizedType) type).getAnnotatedActualTypeArguments();
    if (typeArgs.length != 1) {
      throw new IllegalStateException("A List should have one type argument");
    }

    AnnotatedType valueType = typeArgs[0];
    TypeCodec valueCodec = context.codecFor(valueType);
    if (valueCodec == null) {
      throw new IllegalStateException("No codec registered for type " + valueType);
    }

    List<Object> result = new ArrayList<>();
    for (Object entry : valueAsList) {
      result.add(valueCodec.serialize(valueType, entry, context));
    }
    return result;
  }
}
