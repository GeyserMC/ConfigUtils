package org.geysermc.configutils.node.codec.type;

import java.lang.reflect.AnnotatedType;
import java.util.Locale;
import java.util.UUID;
import org.geysermc.configutils.node.context.NodeContext;

public final class UuidCodec extends TypeCodec<UUID> {
  public static final UuidCodec INSTANCE = new UuidCodec();

  private UuidCodec() {
    super(UUID.class);
  }

  @Override
  public UUID deserialize(AnnotatedType ignored, Object value, NodeContext ignored1) {
    String uuidString = value.toString();
    // support reading compact UUIDs
    if (uuidString.length() == 32) {
      StringBuilder fullUuid = new StringBuilder(uuidString);
      fullUuid.insert(8 + 4 + 4 + 4, '-');
      fullUuid.insert(8 + 4 + 4, '-');
      fullUuid.insert(8 + 4, '-');
      fullUuid.insert(8, '-');
      uuidString = fullUuid.toString();
    }
    return UUID.fromString(uuidString);
  }

  @Override
  public Object serialize(AnnotatedType ignored, UUID value, NodeContext ignored1) {
    return value.toString().toLowerCase(Locale.ROOT);
  }
}
