package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import java.util.Locale;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.util.Utils;

public final class EnumCodec extends TypeCodec<Enum<?>> {
  public static final EnumCodec INSTANCE = new EnumCodec();

  private EnumCodec() {
    super(new TypeToken<Enum<?>>() {});
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Enum<?> deserialize(AnnotatedType type, Object value, RegisteredCodecs ignored) {
    Enum<?> deserialized;
    String upperCase = value.toString().toUpperCase(Locale.ROOT);
    Class<? extends Enum> typeAsEnum =
        GenericTypeReflector.erase(type.getType()).asSubclass(Enum.class);

    try {
      deserialized = Enum.valueOf(typeAsEnum, upperCase.replace('-', '_'));
    } catch (IllegalArgumentException ignored1) {
      deserialized = Enum.valueOf(typeAsEnum, upperCase);
    }
    return deserialized;
  }

  @Override
  public Object serialize(AnnotatedType ignored, Enum<?> value, RegisteredCodecs ignored1) {
    return Utils.constantCaseToKebabCase(value.name());
  }
}
