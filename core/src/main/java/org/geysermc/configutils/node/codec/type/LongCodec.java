package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.context.NodeContext;

public final class LongCodec extends TypeCodec<Long> {
  public static final LongCodec INSTANCE = new LongCodec();

  private LongCodec() {
    super(Long.class);
  }

  @Override
  public Long deserialize(AnnotatedType type, Object value, NodeContext ignored) {
    if (value instanceof Number) {
      return ((Number) value).longValue();
    }

    if (value instanceof String) {
      try {
        return Long.parseLong((String) value);
      } catch (NumberFormatException ignored1) {
        throw new IllegalStateException("String value " + value + " could not be parsed to a long");
      }
    }

    throw new IllegalStateException(
        String.format("Couldn't convert %s with type %s to a long", value, type)
    );
  }

  @Override
  public Object serialize(AnnotatedType ignored, Long value, NodeContext ignored1) {
    return value;
  }
}
