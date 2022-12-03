package org.geysermc.configutils.node.codec.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UuidCodecTest {
  private final UUID toMatch = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
  private TypeCodec<UUID> typeCodec;

  @BeforeEach
  public void setupCodec() {
    typeCodec = UuidCodec.INSTANCE;
  }

  @Test
  public void fullUuidLowerCase() {
    assertEquals(toMatch, deserialize("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
  }

  @Test
  public void fullUuidUpperCase() {
    assertEquals(toMatch, deserialize("AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE"));
  }

  @Test
  public void fullUuidMixedCase() {
    assertEquals(toMatch, deserialize("AaaaaaAA-bbBB-CCcC-DDdd-EEEEEEEeEEEE"));
  }

  @Test
  public void minimalUuidLowerCase() {
    assertEquals(toMatch, deserialize("aaaaaaaabbbbccccddddeeeeeeeeeeee"));
  }

  @Test
  public void minimalUuidUpperCase() {
    assertEquals(toMatch, deserialize("AAAAAAAABBBBCCCCDDDDEEEEEEEEEEEE"));
  }

  @Test
  public void minimalUuidMixedCase() {
    assertEquals(toMatch, deserialize("AaaaaaAAbbBBCCcCDDddEEEEEEEeEEEE"));
  }

  @Test
  public void serializeUuid() {
    assertEquals("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", serialize(toMatch));
  }

  private UUID deserialize(String toDeserialize) {
    // type and codecs aren't used in UUID
    return typeCodec.deserialize(null, toDeserialize, null);
  }

  private String serialize(UUID toSerialize) {
    // type and codecs aren't used in UUID
    return (String) typeCodec.serialize(null, toSerialize, null);
  }
}
