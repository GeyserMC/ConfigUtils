package org.geysermc.configutils.node.codec.type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.UUID;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArrayCodecTest {
  private TypeCodec<?> typeCodec;

  @BeforeEach
  public void setupCodec() {
    typeCodec = ArrayCodec.INSTANCE;
  }

  @Test
  public void deserializeEmptyIntPrimitive() {
    RegisteredCodecs codecs = withInt();

    String[] source = new String[] {};
    int[] expected = new int[] {};

    assertArrayEquals(expected, deserialize(expected.getClass(), source, codecs));
  }

  @Test
  public void deserializeWithUnregisteredComponentCodec() {
    RegisteredCodecs codecs = RegisteredCodecs.builder().build();

    assertEquals(
        "No codec registered for type int",
        assertThrowsExactly(
            IllegalStateException.class,
            () -> deserialize(int[].class, new String[] {}, codecs)
        ).getMessage()
    );
  }

  @Test
  public void deserializeSingleIntPrimitive() {
    RegisteredCodecs codecs = withInt();

    String[] source = new String[] {"55"};
    int[] expected = new int[] {55};

    assertArrayEquals(expected, deserialize(expected.getClass(), source, codecs));
  }

  @Test
  public void deserializeSingleIntBoxed() {
    RegisteredCodecs codecs = withInt();

    String[] source = new String[] {"55"};
    Integer[] expected = new Integer[] {55};

    assertArrayEquals(expected, deserialize(expected.getClass(), source, codecs));
  }

  @Test
  public void deserializeSingleDoublePrimitive() {
    RegisteredCodecs codecs = withDouble();

    String[] source = new String[] {"55.10002"};
    double[] expected = new double[] {55.10002};

    assertArrayEquals(expected, deserialize(expected.getClass(), source, codecs));
  }

  @Test
  public void deserializeSingleDoubleBoxed() {
    RegisteredCodecs codecs = withDouble();

    String[] source = new String[] {"55.10003"};
    Double[] expected = new Double[] {55.10003};

    assertArrayEquals(expected, deserialize(expected.getClass(), source, codecs));
  }

  @Test
  public void deserializeSingleUuid() {
    RegisteredCodecs codecs = withUuid();

    String[] source = new String[] {"aaaaaaaabbbbccccddddeeeeeeeeeeee"};
    UUID[] expected = new UUID[] {UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")};

    assertArrayEquals(expected, deserialize(expected.getClass(), source, codecs));
  }

  @Test
  public void deserializeMultipleIntPrimitive() {
    RegisteredCodecs codecs = withInt();

    String[] source = new String[] {"55", "-123", "4444"};
    int[] expected = new int[] {55, -123, 4444};

    assertArrayEquals(expected, deserialize(expected.getClass(), source, codecs));
  }

  @Test
  public void deserializeMultipleUuid() {
    RegisteredCodecs codecs = withUuid();

    String[] source = new String[] {
        "aaaaaaaabbbbccccddddeeeeeeeeeeee",
        "eaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
    };
    UUID[] expected = new UUID[] {
        UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"),
        UUID.fromString("eaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")
    };

    assertArrayEquals(expected, deserialize(expected.getClass(), source, codecs));
  }

  private <T> T deserialize(Class<T> clazz, Object[] source, RegisteredCodecs codecs) {
    return TypeUtils.deserialize(typeCodec, clazz, source, codecs);
  }

  private RegisteredCodecs withInt() {
    return RegisteredCodecs.builder()
        .registerPrimitive(IntegerCodec.INSTANCE)
        .build();
  }

  private RegisteredCodecs withDouble() {
    return RegisteredCodecs.builder()
        .registerPrimitive(DoubleCodec.INSTANCE)
        .build();
  }

  private RegisteredCodecs withUuid() {
    return RegisteredCodecs.builder()
        .register(UuidCodec.INSTANCE)
        .build();
  }
}
