package org.geysermc.configutils.node.codec.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.leangen.geantyref.GenericTypeReflector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnumCodecTest {
  private TypeCodec<Enum<?>> enumTypeCodec;

  public enum EnumTest {
    ABC, D_E_F
  }

  @BeforeEach
  public void setupCodec() {
    enumTypeCodec = EnumCodec.INSTANCE;
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

  @SuppressWarnings("unchecked")
  private <T extends Enum<T>> Enum<T> deserialize(Class<T> type, String value) {
    return (Enum<T>) enumTypeCodec.deserialize(GenericTypeReflector.annotate(type), value, null);
  }

  private <T extends Enum<T>> Object serialize(Enum<T> type) {
    return enumTypeCodec.serialize(null, type, null);
  }
}
