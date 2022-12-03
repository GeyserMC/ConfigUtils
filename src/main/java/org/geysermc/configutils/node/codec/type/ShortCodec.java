package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.codec.RegisteredCodecs;

public final class ShortCodec extends TypeCodec<Short> {
  public static final ShortCodec INSTANCE = new ShortCodec();

  private ShortCodec() {
    super(Short.class);
  }

  @Override
  public Short deserialize(AnnotatedType type, Object value, RegisteredCodecs ignored) {
    if (value instanceof Number) {
      long asLong = ((Number) value).longValue();
      if (asLong < Short.MIN_VALUE || asLong > Short.MAX_VALUE) {
        throw new IllegalStateException("Short value " + value + " is out of range!");
      }
      return (short) asLong;
    }

    if (value instanceof String) {
      try {
        return Short.parseShort((String) value);
      } catch (NumberFormatException ignored1) {
        throw new IllegalStateException("String value " + value + " could not be parsed to a short");
      }
    }

    throw new IllegalStateException(
        String.format("Couldn't convert %s with type %s to a short", value, type)
    );
  }

  @Override
  public Object serialize(AnnotatedType ignored, Short value, RegisteredCodecs ignored1) {
    return value;
  }
}
