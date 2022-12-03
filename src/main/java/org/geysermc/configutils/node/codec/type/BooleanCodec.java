package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.codec.RegisteredCodecs;

public final class BooleanCodec extends TypeCodec<Boolean> {
  public static final BooleanCodec INSTANCE = new BooleanCodec();

  private BooleanCodec() {
    super(Boolean.class);
  }

  @Override
  public Boolean deserialize(AnnotatedType type, Object value, RegisteredCodecs ignored) {
    if (value instanceof Boolean) {
      return (boolean) value;
    }
    if (value instanceof Number) {
      long asLong = ((Number) value).longValue();
      if (asLong < 0 || asLong > 1) {
        throw new IllegalStateException("Value " + value + " out of range for boolean");
      }
      return asLong == 1;
    }
    if (value instanceof String) {
      switch (((String) value).toLowerCase()) {
        case "true":
        case "yes":
        case "y":
        case "1":
          return true;
        case "false":
        case "no":
        case "n":
        case "0":
          return false;
        default:
          throw new IllegalStateException("Cannot convert string " + value + " to a boolean");
      }
    }
    throw new IllegalStateException(
        String.format("Couldn't convert %s with type %s to a string", value, type)
    );
  }

  @Override
  public Object serialize(AnnotatedType ignored, Boolean value, RegisteredCodecs ignored1) {
    return value;
  }
}
