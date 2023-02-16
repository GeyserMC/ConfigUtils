package org.geysermc.configutils.node.codec.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectCodecTest {
  private TypeCodec<?> typeCodec;

  @BeforeEach
  public void setupCodec() {
    typeCodec = ObjectCodec.INSTANCE;
  }

  @Test
  public void deserializeSimpleProxied() {
    RegisteredCodecs codecs =
        RegisteredCodecs.builder()
            .registerPrimitive(IntegerCodec.INSTANCE)
            .registerPrimitive(BooleanCodec.INSTANCE)
            .registerGreedy(typeCodec)
            .build();

    Map<Object, Object> data = new HashMap<>();
    data.put("a", "555");
    data.put("long-name", "true");

    SimpleProxiedInterface result = deserialize(SimpleProxiedInterface.class, data, codecs);

    assertNotNull(result);
    assertEquals(555, result.a());
    assertTrue(result.longName());
  }

  @Test
  public void deserializeSimpleExtendedProxied() {
    RegisteredCodecs codecs =
        RegisteredCodecs.builder()
            .registerPrimitive(IntegerCodec.INSTANCE)
            .registerPrimitive(BooleanCodec.INSTANCE)
            .register(StringCodec.INSTANCE)
            .registerGreedy(typeCodec)
            .build();

    Map<Object, Object> data = new HashMap<>();
    data.put("a", "555");
    data.put("long-name", "true");
    data.put("beta", "yas");

    SimpleExtendedProxiedInterface result =
        deserialize(SimpleExtendedProxiedInterface.class, data, codecs);

    assertNotNull(result);
    assertEquals(555, result.a());
    assertTrue(result.longName());
    assertEquals("yas", result.beta());
  }

  @Test
  public void deserializeComplexProxied() {
    RegisteredCodecs codecs =
        RegisteredCodecs.builder()
            .registerPrimitive(IntegerCodec.INSTANCE)
            .registerPrimitive(BooleanCodec.INSTANCE)
            .registerPrimitive(ByteCodec.INSTANCE)
            .register(StringCodec.INSTANCE)
            .registerGreedy(typeCodec)
            .build();

    Map<Object, Object> dataInner = new HashMap<>();
    dataInner.put("yellow", "is a color");
    dataInner.put("a-bridge", "22");

    Map<Object, Object> dataOuter = new HashMap<>();
    dataOuter.put("b", "444");
    dataOuter.put("not-so-short-name", "true");
    dataOuter.put("inner", dataInner);

    ComplexProxiedInterface result = deserialize(ComplexProxiedInterface.class, dataOuter, codecs);

    assertNotNull(result);
    assertEquals(444, result.b());
    assertTrue(result.notSoShortName());

    InnerProxiedInterface inner = result.inner();
    assertNotNull(inner);
    assertEquals("is a color", inner.yellow());
    assertEquals((byte) 22, inner.aBridge());
  }

  private <T> T deserialize(Class<T> type, Map<?, ?> data, RegisteredCodecs codecs) {
    return TypeUtils.deserialize(typeCodec, type, data, codecs);
  }

  public interface SimpleProxiedInterface {
    int a();
    boolean longName();
  }

  public interface SimpleExtendedProxiedInterface extends SimpleProxiedInterface {
    String beta();
  }

  public interface ComplexProxiedInterface {
    int b();
    InnerProxiedInterface inner();
    boolean notSoShortName();
  }

  public interface InnerProxiedInterface {
    String yellow();
    byte aBridge();
  }
}
