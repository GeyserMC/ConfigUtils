package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.codec.RegisteredCodecs;

public final class FloatCodec extends TypeCodec<Float> {
  public static final FloatCodec INSTANCE = new FloatCodec();

  private FloatCodec() {
    super(Float.class);
  }

  @Override
  public Float deserialize(AnnotatedType type, Object value, RegisteredCodecs ignored) {
    if (value instanceof Number) {
      double asDouble = ((Number) value).doubleValue();
      if (asDouble < Float.MIN_VALUE || asDouble > Float.MAX_VALUE) {
        throw new IllegalStateException("Float value " + value + " is out of range!");
      }
      return (float) asDouble;
    }

    if (value instanceof String) {
      try {
        return Float.parseFloat((String) value);
      } catch (NumberFormatException ignored1) {
        throw new IllegalStateException("String value " + value + " could not be parsed to a float");
      }
    }

    throw new IllegalStateException(
        String.format("Couldn't convert %s with type %s to a float", value, type)
    );
  }

  @Override
  public Object serialize(AnnotatedType ignored, Float value, RegisteredCodecs ignored1) {
    return value;
  }
}
