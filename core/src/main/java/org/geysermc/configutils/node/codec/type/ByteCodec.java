package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.context.NodeContext;

public final class ByteCodec extends TypeCodec<Byte> {
  public static final ByteCodec INSTANCE = new ByteCodec();

  private ByteCodec() {
    super(Byte.class);
  }

  @Override
  public Byte deserialize(AnnotatedType type, Object value, NodeContext ignored) {
    if (value instanceof Number) {
      long asLong = ((Number) value).longValue();
      if (asLong < Byte.MIN_VALUE || asLong > Byte.MAX_VALUE) {
        throw new IllegalStateException("Byte value " + value + " is out of range!");
      }
      return (byte) asLong;
    }

    if (value instanceof String) {
      try {
        return Byte.parseByte((String) value);
      } catch (NumberFormatException ignored1) {
        throw new IllegalStateException("String value " + value + " could not be parsed to a byte");
      }
    }

    throw new IllegalStateException(
        String.format("Couldn't convert %s with type %s to a byte", value, type)
    );
  }

  @Override
  public Object serialize(AnnotatedType ignored, Byte value, NodeContext ignored1) {
    return value;
  }
}
