package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.codec.RegisteredCodecs;

public final class DoubleCodec extends TypeCodec<Double> {
  public static final DoubleCodec INSTANCE = new DoubleCodec();

  private DoubleCodec() {
    super(Double.class);
  }

  @Override
  public Double deserialize(AnnotatedType type, Object value, RegisteredCodecs ignored) {
    if (value instanceof Number) {
      return ((Number) value).doubleValue();
    }

    if (value instanceof String) {
      try {
        return Double.parseDouble((String) value);
      } catch (NumberFormatException ignored1) {
        throw new IllegalStateException("String value " + value + " could not be parsed to a float");
      }
    }

    throw new IllegalStateException(
        String.format("Couldn't convert %s with type %s to a float", value, type)
    );
  }

  @Override
  public Object serialize(AnnotatedType ignored, Double value, RegisteredCodecs ignored1) {
    return value;
  }
}
