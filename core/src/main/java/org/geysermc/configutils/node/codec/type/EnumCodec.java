package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.context.NodeContext;

public final class EnumCodec extends TypeCodec<Enum<?>> {
  public static final EnumCodec INSTANCE = new EnumCodec();

  private EnumCodec() {
    super(new TypeToken<Enum<?>>() {});
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Enum<?> deserialize(AnnotatedType type, Object value, NodeContext context) {
    Class<? extends Enum> typeAsEnum =
        GenericTypeReflector.erase(type.getType()).asSubclass(Enum.class);

    if (value instanceof Number) {
      long ordinal = ((Number) value).longValue();
      if (ordinal < 0 || ordinal > Integer.MAX_VALUE) {
        throw new IllegalStateException("Enum ordinal " + ordinal + " is out of range!");
      }
      Enum<?>[] constants = typeAsEnum.getEnumConstants();
      if (ordinal >= constants.length) {
        throw new IllegalStateException(
            String.format("Enum ordinal %d is out of range for %s!", ordinal, type)
        );
      }
      return constants[(int) ordinal];
    }

    Enum<?> deserialized;
    String rawName = value.toString();
    String correctName = context.options().codec().enumDecoder().apply(rawName);

    try {
      deserialized = Enum.valueOf(typeAsEnum, correctName);
    } catch (IllegalArgumentException ignored) {
      throw new IllegalStateException(
          String.format("Could not find %s (decoded as: %s) in %s", rawName, correctName, type)
      );
    }
    return deserialized;
  }

  @Override
  public Object serialize(AnnotatedType ignored, Enum<?> value, NodeContext ignored1) {
    return ignored1.options().codec().enumEncoder().apply(value.name());
  }
}
