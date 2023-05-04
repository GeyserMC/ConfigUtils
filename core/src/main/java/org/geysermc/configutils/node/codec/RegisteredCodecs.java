package org.geysermc.configutils.node.codec;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.geysermc.configutils.node.codec.type.ArrayCodec;
import org.geysermc.configutils.node.codec.type.BooleanCodec;
import org.geysermc.configutils.node.codec.type.ByteCodec;
import org.geysermc.configutils.node.codec.type.DoubleCodec;
import org.geysermc.configutils.node.codec.type.EnumCodec;
import org.geysermc.configutils.node.codec.type.FloatCodec;
import org.geysermc.configutils.node.codec.type.IntegerCodec;
import org.geysermc.configutils.node.codec.type.ListCodec;
import org.geysermc.configutils.node.codec.type.LongCodec;
import org.geysermc.configutils.node.codec.type.MapCodec;
import org.geysermc.configutils.node.codec.type.ObjectCodec;
import org.geysermc.configutils.node.codec.type.SetCodec;
import org.geysermc.configutils.node.codec.type.ShortCodec;
import org.geysermc.configutils.node.codec.type.StringCodec;
import org.geysermc.configutils.node.codec.type.TypeCodec;
import org.geysermc.configutils.node.codec.type.UuidCodec;

public class RegisteredCodecs {
  private static final RegisteredCodecs DEFAULT;

  static {
    DEFAULT = builder()
        .registerPrimitive(BooleanCodec.INSTANCE)
        .registerPrimitive(ByteCodec.INSTANCE)
        .registerPrimitive(ShortCodec.INSTANCE)
        .registerPrimitive(IntegerCodec.INSTANCE)
        .registerPrimitive(LongCodec.INSTANCE)
        .registerPrimitive(FloatCodec.INSTANCE)
        .registerPrimitive(DoubleCodec.INSTANCE)
        .registerExact(StringCodec.INSTANCE)
        .registerExact(UuidCodec.INSTANCE)
        .register(ArrayCodec.MATCH, ArrayCodec.INSTANCE)
        .register(ListCodec.TYPE, ListCodec.INSTANCE)
        .register(SetCodec.TYPE, SetCodec.INSTANCE)
        .register(MapCodec.TYPE, MapCodec.INSTANCE)
        .register(EnumCodec.INSTANCE)
        .registerGreedy(ObjectCodec.REFLECTION_PROXY_INSTANCE)
        .build();
  }

  private final List<RegisteredTypeCodec> registeredCodecs;
  private final TypeCodec<?> greedyCodec;

  private RegisteredCodecs(List<RegisteredTypeCodec> registeredCodecs, TypeCodec<?> greedyCodec) {
    this.registeredCodecs = new ArrayList<>(registeredCodecs);
    this.greedyCodec = greedyCodec;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static RegisteredCodecs defaults() {
    return DEFAULT;
  }

  public Builder toBuilder() {
    return new Builder(new ArrayList<>(registeredCodecs), greedyCodec);
  }

  @SuppressWarnings("unchecked")
  public <T> TypeCodec<T> get(Type type) {
    for (RegisteredTypeCodec registeredCodec : registeredCodecs) {
      if (registeredCodec.matches(type)) {
        return (TypeCodec<T>) registeredCodec.codec();
      }
    }
    return (TypeCodec<T>) greedyCodec;
  }

  public <T> TypeCodec<T> get(AnnotatedType type) {
    // we don't use the AnnotatedType for actual matching,
    // but we do use the AnnotatedType for de-/serialization
    return get(type.getType());
  }

  public static class Builder {
    private final List<RegisteredTypeCodec> registeredCodecs;
    private TypeCodec<?> greedyCodec;

    private Builder(List<RegisteredTypeCodec> registeredCodecs, TypeCodec<?> greedyCodec) {
      this.registeredCodecs = registeredCodecs;
      this.greedyCodec = greedyCodec;
    }

    private Builder() {
      this(new ArrayList<>(), null);
    }

    public <T> Builder register(TypeCodec<T> codec) {
      return register(codec.type(), codec);
    }

    public <T> Builder register(TypeToken<T> typeToken, TypeCodec<? super T> codec) {
      return register(typeToken.getType(), codec);
    }

    public Builder register(Type type, TypeCodec<?> codec) {
      // e.g. HashMap should match Map, but Object shouldn't match Map
      return register(test -> GenericTypeReflector.isSuperType(type, test), codec);
    }

    public Builder register(Predicate<Type> predicate, TypeCodec<?> codec) {
      registeredCodecs.add(new RegisteredTypeCodec(predicate, codec));
      return this;
    }

    public <T> Builder registerPrimitive(TypeCodec<T> codec) {
      return registerPrimitive(codec.type(), codec);
    }

    public <T> Builder registerPrimitive(TypeToken<T> typeToken, TypeCodec<? super T> codec) {
      return registerPrimitive(typeToken.getType(), codec);
    }

    public Builder registerPrimitive(Type type, TypeCodec<?> codec) {
      // Integer autoboxes to int, so we only have to check if the boxed variant matches
      return register(test -> type.equals(GenericTypeReflector.box(test)), codec);
    }

    public <T> Builder registerExact(TypeCodec<T> codec) {
      return registerExact(codec.type(), codec);
    }

    public <T> Builder registerExact(TypeToken<T> typeToken, TypeCodec<? super T> codec) {
      return registerExact(typeToken.getType(), codec);
    }

    public Builder registerExact(Type type, TypeCodec<?> codec) {
      registeredCodecs.add(new RegisteredTypeCodec(type, codec));
      return this;
    }

    public Builder registerGreedy(TypeCodec<?> codec) {
      greedyCodec = codec;
      return this;
    }

    public RegisteredCodecs build() {
      return new RegisteredCodecs(registeredCodecs, greedyCodec);
    }
  }

  private static class RegisteredTypeCodec {
    private final Predicate<Type> predicate;
    private final TypeCodec<?> codec;

    private RegisteredTypeCodec(Predicate<Type> predicate, TypeCodec<?> codec) {
      this.predicate = predicate;
      this.codec = codec;
    }

    private RegisteredTypeCodec(Type exactMatch, TypeCodec<?> codec) {
      this.predicate = test -> test.equals(exactMatch);
      this.codec = codec;
    }

    public TypeCodec<?> codec() {
      return codec;
    }

    public boolean matches(Type toMatch) {
      return predicate.test(toMatch);
    }
  }
}
