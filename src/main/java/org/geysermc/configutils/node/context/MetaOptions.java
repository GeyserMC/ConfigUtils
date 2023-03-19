package org.geysermc.configutils.node.context;

import static io.leangen.geantyref.GenericTypeReflector.annotate;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.node.codec.type.TypeCodec;
import org.geysermc.configutils.node.meta.Comment;
import org.geysermc.configutils.node.meta.DefaultOnFailure;
import org.geysermc.configutils.node.meta.Defaults.DefaultBoolean;
import org.geysermc.configutils.node.meta.Defaults.DefaultDecimal;
import org.geysermc.configutils.node.meta.Defaults.DefaultNumeric;
import org.geysermc.configutils.node.meta.Defaults.DefaultString;
import org.geysermc.configutils.node.meta.Range;

public class MetaOptions {
  private final AnnotatedType type;
  private final NodeContext context;
  private final TypeCodec<?> typeCodec;

  public MetaOptions(NodeContext context, AnnotatedType type) {
    this.type = type;
    this.context = context;
    this.typeCodec = context.codecFor(type);
    Objects.requireNonNull(
        typeCodec,
        "Cannot retrieve meta options for non-registered type " + type
    );
  }

  public Object applyMeta(Object value) {
    return null;
  }

  public @Nullable String comment() {
    Comment comment = type.getAnnotation(Comment.class);
    return comment != null ? comment.value() : null;
  }

  public @Nullable Object defaultValue() {
    DefaultString string = type.getAnnotation(DefaultString.class);
    if (string != null) {
      return string.value();
    }

    DefaultNumeric numeric = type.getAnnotation(DefaultNumeric.class);
    if (numeric != null) {
      return numeric.value();
    }

    DefaultDecimal decimal = type.getAnnotation(DefaultDecimal.class);
    if (decimal != null) {
      return decimal.value();
    }

    DefaultBoolean bool = type.getAnnotation(DefaultBoolean.class);
    if (bool != null) {
      return bool.value();
    }
    return null;
  }

  public @Nullable Object deserializedDefaultValue() {
    try {
      Object defaultValue = defaultValue();
      if (defaultValue == null) {
        return null;
      }
      return typeCodec.deserialize(annotate(defaultValue.getClass()), defaultValue, context);
    } catch (IllegalStateException exception) {
      throw new IllegalStateException("Failed to retrieve default value for " + type, exception);
    }
  }

  public boolean defaultOnFailure() {
    return type.getAnnotation(DefaultOnFailure.class) != null;
  }

  public Range range() {
    return Range.of(type);
  }

  public boolean isInRange(Object value) {
    return range().isInRange(value);
  }

  public AnnotatedType type() {
    return type;
  }

  public TypeCodec<?> typeCodec() {
    return typeCodec;
  }
}
