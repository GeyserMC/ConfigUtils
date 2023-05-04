package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.node.context.NodeContext;
import org.geysermc.configutils.node.context.RootNodeContext;
import org.geysermc.configutils.node.context.option.NodeOptions;

public class TypeUtils {
  public static NodeContext createContext(RegisteredCodecs codecs, AnnotatedType type) {
    return new RootNodeContext(codecs, NodeOptions.defaults(), type);
  }

  public static <T> T deserialize(
      TypeCodec<?> codec,
      Class<T> type,
      Object data,
      RegisteredCodecs codecs
  ) {
    return deserialize(codec, GenericTypeReflector.annotate(type), data, codecs);
  }

  public static <T> T deserialize(
      TypeCodec<?> codec,
      TypeToken<T> type,
      Object data,
      RegisteredCodecs codecs
  ) {
    return deserialize(codec, type.getCanonicalType(), data, codecs);
  }

  @SuppressWarnings("unchecked")
  public static <T> T deserialize(
      TypeCodec<?> codec,
      AnnotatedType type,
      Object data,
      RegisteredCodecs codecs
  ) {
    return (T) codec.deserialize(type, data, createContext(codecs, type));
  }

  public static <T> T serialize(
      TypeCodec<?> codec,
      Class<?> type,
      Object data,
      RegisteredCodecs codecs
  ) {
    return serialize(codec, GenericTypeReflector.annotate(type), data, codecs);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T> T serialize(
      TypeCodec codec,
      AnnotatedType type,
      Object data,
      RegisteredCodecs codecs
  ) {
    return (T) codec.serialize(type, data, createContext(codecs, type));
  }
}
