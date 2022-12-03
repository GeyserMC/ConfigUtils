package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.function.Predicate;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.util.TypeUtils;

public final class ArrayCodec extends TypeCodec<Object> {
  public static final ArrayCodec INSTANCE = new ArrayCodec();
  public static final Predicate<Type> MATCH = TypeUtils::isArray;

  private ArrayCodec() {
    super(Object.class);
  }

  @Override
  public Object deserialize(AnnotatedType type, Object value, RegisteredCodecs codecs) {
    int length = Array.getLength(value);

    AnnotatedType componentType = GenericTypeReflector.getArrayComponentType(type);
    if (componentType == null) {
      throw new IllegalStateException("Huh? Type " + type + " doesn't have an component type?");
    }
    TypeCodec<?> typeCodec = codecs.get(componentType);
    if (typeCodec == null) {
      throw new IllegalStateException("No codec registered for type " + componentType);
    }

    Object array = Array.newInstance(GenericTypeReflector.erase(componentType.getType()), length);
    for (int i = 0; i < length; i++) {
      Object entry = Array.get(value, i);
      Array.set(array, i, typeCodec.deserialize(componentType, entry, codecs));
    }
    return array;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Object serialize(AnnotatedType type, Object value, RegisteredCodecs codecs) {
    int length = Array.getLength(value);

    AnnotatedType componentType = GenericTypeReflector.getArrayComponentType(type);
    if (componentType == null) {
      throw new IllegalStateException("Huh? Type " + type + " doesn't have an component type?");
    }
    TypeCodec typeCodec = codecs.get(componentType);
    if (typeCodec == null) {
      throw new IllegalStateException("No codec registered for type " + componentType);
    }

    Object array = Array.newInstance(GenericTypeReflector.erase(componentType.getType()), length);
    for (int i = 0; i < length; i++) {
      Object entry = Array.get(value, i);
      Array.set(array, i, typeCodec.serialize(componentType, entry, codecs));
    }
    return array;
  }
}
