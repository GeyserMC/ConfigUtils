package org.geysermc.configutils.node.codec.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import io.leangen.geantyref.TypeToken;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MapCodecTest {
  private TypeCodec<?> typeCodec;

  @BeforeEach
  public void setupCodec() {
    typeCodec = MapCodec.INSTANCE;
  }

  @Test
  public void deserializeEmptyMap() {
    RegisteredCodecs codecs = withString();

    Map<String, String> emptyMap = new HashMap<>();
    Map<String, String> deserialized =
        deserialize(new TypeToken<Map<String, String>>() {}, emptyMap, codecs);

    assertNotSame(emptyMap, deserialized);
    assertEquals(emptyMap, deserialized);
  }

  @Test
  public void deserializeWithUnregisteredComponentType() {
    RegisteredCodecs codecs = withString();

    assertEquals(
        "No codec registered for type java.util.UUID",
        assertThrowsExactly(
            IllegalStateException.class,
            () -> deserialize(new TypeToken<Map<UUID, String>>() {}, Collections.emptyMap(), codecs)
        ).getMessage()
    );
    assertEquals(
        "No codec registered for type java.util.UUID",
        assertThrowsExactly(
            IllegalStateException.class,
            () -> deserialize(new TypeToken<Map<String, UUID>>() {}, Collections.emptyMap(), codecs)
        ).getMessage()
    );
  }

  @Test
  public void deserializeSingleUuidInt() {
    RegisteredCodecs codecs = withUuidAndInteger();

    Map<String, String> source = new HashMap<>();
    source.put("aaaaaaaabbbbccccddddeeeeeeeeeeee", "123");

    Map<UUID, Integer> result = new HashMap<>();
    result.put(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), 123);

    assertEquals(result, deserialize(new TypeToken<Map<UUID, Integer>>() {}, source, codecs));
  }

  @Test
  public void deserializeMultipleUuidInt() {
    RegisteredCodecs codecs = withUuidAndInteger();

    Map<String, String> source = new HashMap<>();
    source.put("aaaaaaaabbbbccccddddeeeeeeeeeeee", "123");
    source.put("eaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "-242");

    Map<UUID, Integer> result = new HashMap<>();
    result.put(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), 123);
    result.put(UUID.fromString("eaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), -242);

    assertEquals(result, deserialize(new TypeToken<Map<UUID, Integer>>() {}, source, codecs));
  }

  private <K, V> Map<K, V> deserialize(
      TypeToken<Map<K, V>> type,
      Map<?, ?> value,
      RegisteredCodecs codecs
  ) {
    return TypeUtils.deserialize(typeCodec, type, value, codecs);
  }

  private RegisteredCodecs withString() {
    return RegisteredCodecs.builder()
        .register(StringCodec.INSTANCE)
        .build();
  }

  private RegisteredCodecs withUuidAndInteger() {
    return RegisteredCodecs.builder()
        .register(UuidCodec.INSTANCE)
        .register(IntegerCodec.INSTANCE)
        .build();
  }
}
