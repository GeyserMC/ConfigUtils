package org.geysermc.configutils.node.codec;

import static io.leangen.geantyref.GenericTypeReflector.annotate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.geysermc.configutils.node.codec.type.ByteCodec;
import org.geysermc.configutils.node.codec.type.ShortCodec;
import org.geysermc.configutils.node.codec.type.StringCodec;
import org.junit.jupiter.api.Test;

public class RegisteredCodecsTest {
  @Test
  @SuppressWarnings("AssertBetweenInconvertibleTypes")
  public void registerPrimitiveCodec() {
    RegisteredCodecs codecs =
        RegisteredCodecs.builder()
            .registerPrimitive(ByteCodec.INSTANCE)
            .build();

    assertEquals(ByteCodec.INSTANCE, codecs.get(byte.class));
    assertEquals(ByteCodec.INSTANCE, codecs.get(Byte.class));
    assertEquals(ByteCodec.INSTANCE, codecs.get(annotate(byte.class)));
    assertEquals(ByteCodec.INSTANCE, codecs.get(annotate(Byte.class)));
    assertNull(codecs.get(Object.class));
  }

  @Test
  @SuppressWarnings("AssertBetweenInconvertibleTypes")
  public void registerGreedy() {
    RegisteredCodecs codecs =
        RegisteredCodecs.builder()
            .registerPrimitive(ByteCodec.INSTANCE)
            .registerGreedy(StringCodec.INSTANCE)
            .build();

    assertEquals(ByteCodec.INSTANCE, codecs.get(byte.class));
    assertEquals(ByteCodec.INSTANCE, codecs.get(Byte.class));
    assertEquals(ByteCodec.INSTANCE, codecs.get(annotate(byte.class)));
    assertEquals(ByteCodec.INSTANCE, codecs.get(annotate(Byte.class)));
    assertEquals(StringCodec.INSTANCE, codecs.get(Object.class));
    assertEquals(StringCodec.INSTANCE, codecs.get(short.class));
    assertEquals(StringCodec.INSTANCE, codecs.get(int.class));
  }

  @Test
  @SuppressWarnings("AssertBetweenInconvertibleTypes")
  public void extendRegisteredCodecs() {
    RegisteredCodecs initialCodecs =
        RegisteredCodecs.builder()
            .registerPrimitive(ByteCodec.INSTANCE)
            .build();

    assertEquals(ByteCodec.INSTANCE, initialCodecs.get(byte.class));
    assertNull(initialCodecs.get(short.class));

    RegisteredCodecs extendedCodecs =
        initialCodecs.toBuilder()
            .registerPrimitive(ShortCodec.INSTANCE)
            .build();

    assertEquals(ByteCodec.INSTANCE, extendedCodecs.get(byte.class));
    assertEquals(ShortCodec.INSTANCE, extendedCodecs.get(short.class));

    assertEquals(ByteCodec.INSTANCE, initialCodecs.get(byte.class));
    assertNull(initialCodecs.get(short.class));
  }

  @Test
  @SuppressWarnings("AssertBetweenInconvertibleTypes")
  public void extendRegisteredCodecsGreedyOverride() {
    RegisteredCodecs initialCodecs =
        RegisteredCodecs.builder()
            .registerPrimitive(ByteCodec.INSTANCE)
            .registerGreedy(StringCodec.INSTANCE)
            .build();

    assertEquals(ByteCodec.INSTANCE, initialCodecs.get(byte.class));
    assertEquals(StringCodec.INSTANCE, initialCodecs.get(int.class));

    RegisteredCodecs extendedCodecs =
        initialCodecs.toBuilder()
            .registerGreedy(ShortCodec.INSTANCE)
            .build();

    assertEquals(ByteCodec.INSTANCE, extendedCodecs.get(byte.class));
    assertEquals(ShortCodec.INSTANCE, extendedCodecs.get(int.class));

    assertEquals(ByteCodec.INSTANCE, initialCodecs.get(byte.class));
    assertEquals(StringCodec.INSTANCE, initialCodecs.get(int.class));
  }
}