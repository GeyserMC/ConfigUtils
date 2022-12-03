package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.codec.RegisteredCodecs;

public final class StringCodec extends TypeCodec<String> {
  public static final StringCodec INSTANCE = new StringCodec();

  private StringCodec() {
    super(String.class);
  }

  @Override
  public String deserialize(AnnotatedType ignored, Object value, RegisteredCodecs ignored1) {
    return value.toString();
  }

  @Override
  public Object serialize(AnnotatedType ignored, String value, RegisteredCodecs ignored1) {
    return value;
  }
}
