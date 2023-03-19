package org.geysermc.configutils.node.codec.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.geysermc.configutils.node.codec.RegisteredCodecs;
import org.geysermc.configutils.node.meta.Defaults.DefaultBoolean;
import org.geysermc.configutils.node.meta.Defaults.DefaultDecimal;
import org.geysermc.configutils.node.meta.Defaults.DefaultNumeric;
import org.geysermc.configutils.node.meta.Defaults.DefaultString;
import org.geysermc.configutils.node.meta.Range.DecimalRange;
import org.geysermc.configutils.node.meta.Range.NumericRange;
import org.geysermc.configutils.node.meta.Range.StringRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ObjectCodecTest {
  private TypeCodec<?> typeCodec;

  @BeforeEach
  public void setupCodec() {
    typeCodec = ObjectCodec.REFLECTION_PROXY_INSTANCE;
  }

  @Test
  public void deserializeSimpleReflectionProxy() {
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
  public void deserializeSimpleExtendedReflectionProxy() {
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
  public void deserializeComplexReflectionProxy() {
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

  @Test
  public void deserializeSimpleAnnotatedReflectionProxy() {
    RegisteredCodecs codecs =
        RegisteredCodecs.builder()
            .registerPrimitive(BooleanCodec.INSTANCE)
            .registerPrimitive(FloatCodec.INSTANCE)
            .registerPrimitive(IntegerCodec.INSTANCE)
            .register(StringCodec.INSTANCE)
            .registerGreedy(typeCodec)
            .build();

    Map<Object, Object> data = new HashMap<>();

    SimpleAnnotatedProxiedInterface result =
        deserialize(SimpleAnnotatedProxiedInterface.class, data, codecs);

    assertNotNull(result);
    assertTrue(result.longName());
    assertEquals(8.25, result.aDecimal());
    assertEquals(5, result.a());
    assertEquals("oh a string", result.aString());
    assertTrue(result.longName());
  }

  @Test
  public void deserializeRangeAnnotatedReflectionProxy() {
    RegisteredCodecs codecs =
        RegisteredCodecs.builder()
            .registerPrimitive(DoubleCodec.INSTANCE)
            .registerPrimitive(IntegerCodec.INSTANCE)
            .register(StringCodec.INSTANCE)
            .registerGreedy(typeCodec)
            .build();

    Map<Object, Object> data = new HashMap<>();

    IndexOutOfBoundsException exception = assertThrowsExactly(
        IndexOutOfBoundsException.class,
        () -> deserialize(RangeAnnotatedProxiedInterface.class, data, codecs)
    );
    assertEquals(
        "'a too long string' (key: aaaa) is not in the allowed range of from: 0, to: 10!",
        exception.getMessage()
    );
  }

  private <T> T deserialize(Class<T> type, Map<?, ?> data, RegisteredCodecs codecs) {
    return TypeUtils.deserialize(typeCodec, type, data, codecs);
  }

  public interface SimpleProxiedInterface {
    int a();
    boolean longName();
  }

  public interface SimpleAnnotatedProxiedInterface {
    @DefaultBoolean(true)
    boolean longName();
    @DefaultDecimal(8.25)
    float aDecimal();
    @DefaultNumeric(5)
    int a();
    @DefaultString("oh a string")
    String aString();
  }

  // IDK how the internal getMethods order works,
  // but with this current version the order is: aa, aaa, aaaa.
  // so the failing range check is executed last
  public interface RangeAnnotatedProxiedInterface {
    @NumericRange(from = 5, to = 10)
    @DefaultNumeric(5)
    int aa();

    @DecimalRange(from = 7.6, to = 7.9)
    @DefaultDecimal(7.8)
    double aaa();

    @StringRange(from = 0, to = 10)
    @DefaultString("a too long string")
    String aaaa();
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
