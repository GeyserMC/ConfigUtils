package org.geysermc.configutils.node.codec.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.junit.jupiter.api.Test;

public class EnumCodecTest {
  private final EnumCodec codec = EnumCodec.INSTANCE;
  private final RegisteredCodecs register =
      RegisteredCodecs.builder().register(EnumCodec.INSTANCE).build();

  public enum EnumTest {
    ABC, D_E_F
  }

  @Test
  public void nameMatchLowerCase() {
    assertEquals(EnumTest.ABC, deserialize(EnumTest.class, "abc"));
  }

  @Test
  public void nameMatchUpperCase() {
    assertEquals(EnumTest.ABC, deserialize(EnumTest.class, "ABC"));
  }

  @Test
  public void nameMatchMixedCase() {
    assertEquals(EnumTest.ABC, deserialize(EnumTest.class, "aBc"));
  }

  @Test
  public void nameMatchWithDashSeparator() {
    assertEquals(EnumTest.D_E_F, deserialize(EnumTest.class, "d-e-f"));
  }

  @Test
  public void nameMatchWithUnderscoreSeparator() {
    assertEquals(EnumTest.D_E_F, deserialize(EnumTest.class, "d_e_f"));
  }

  @Test
  public void nameMatchWithMixedSeparator() {
    assertEquals(EnumTest.D_E_F, deserialize(EnumTest.class, "d-e_f"));
  }

  @Test
  public void serializeSimple() {
    assertEquals("abc", serialize(EnumTest.ABC));
  }

  @Test
  public void serializeWithSeparator() {
    assertEquals("d-e-f", serialize(EnumTest.D_E_F));
  }

  private <T extends Enum<T>> Enum<T> deserialize(Class<T> type, String value) {
    return TypeUtils.deserialize(codec, type, value, register);
  }

  private <T extends Enum<T>> Object serialize(Enum<T> type) {
    return TypeUtils.serialize(EnumCodec.INSTANCE, type.getClass(), type, register);
  }
}
