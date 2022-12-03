package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.codec.RegisteredCodecs;

public final class IntegerCodec extends TypeCodec<Integer> {
  public static final IntegerCodec INSTANCE = new IntegerCodec();

  private IntegerCodec() {
    super(Integer.class);
  }

  @Override
  public Integer deserialize(AnnotatedType type, Object value, RegisteredCodecs ignored) {
    //todo add option to reject double, float and string values
    if (value instanceof Number) {
      long asLong = ((Number) value).longValue();
      if (asLong < Integer.MIN_VALUE || asLong > Integer.MAX_VALUE) {
        throw new IllegalStateException("Integer value " + value + " is out of range!");
      }
      return (int) asLong;
    }

    if (value instanceof String) {
      try {
        return Integer.parseInt((String) value);
      } catch (NumberFormatException ignored1) {
        throw new IllegalStateException("String value " + value + " could not be parsed to an int");
      }
    }

    throw new IllegalStateException(
        String.format("Couldn't convert %s with type %s to an int", value, type)
    );
  }

  @Override
  public Object serialize(AnnotatedType ignored, Integer value, RegisteredCodecs ignored1) {
    return value;
  }
}
