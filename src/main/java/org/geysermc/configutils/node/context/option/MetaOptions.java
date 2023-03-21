package org.geysermc.configutils.node.context.option;

import static io.leangen.geantyref.GenericTypeReflector.annotate;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.node.codec.type.TypeCodec;
import org.geysermc.configutils.node.context.NodeContext;
import org.geysermc.configutils.node.meta.Comment;
import org.geysermc.configutils.node.meta.DefaultOnFailure;
import org.geysermc.configutils.node.meta.Defaults.DefaultBoolean;
import org.geysermc.configutils.node.meta.Defaults.DefaultDecimal;
import org.geysermc.configutils.node.meta.Defaults.DefaultNumeric;
import org.geysermc.configutils.node.meta.Defaults.DefaultString;
import org.geysermc.configutils.node.meta.Placeholder;
import org.geysermc.configutils.node.meta.Range;

public class MetaOptions {
  private final NodeContext context;

  public MetaOptions(NodeContext context) {
    this.context = context;
  }

  public Object applyMeta(String key, Object value) {
    if (value == null) {
      value = deserializedDefaultValue();
    }

    if (value == null) {
      value = resolvedPlaceholder();
    }

    if (!isInRange(value)) {
      Range range = range();
      //todo DefaultOnFailure & add error handler option
      throw new IndexOutOfBoundsException(String.format(
          "'%s' (key: %s) is not in the allowed range of from: %s, to: %s!",
          value, key, range.from(), range.to()
      ));
    }

    return value;
  }

  public @Nullable String comment() {
    Comment comment = annotation(Comment.class);
    return comment != null ? comment.value() : null;
  }

  public @Nullable String placeholder() {
    Placeholder placeholder = annotation(Placeholder.class);
    if (placeholder == null) {
      return null;
    }

    String placeholderId = placeholder.value();
    if (Placeholder.DEFAULT.equals(placeholderId)) {
      return context.fullKey();
    }
    return placeholderId;
  }

  public @Nullable Object resolvedPlaceholder() {
    String placeholderId = placeholder();
    if (placeholderId == null) {
      return null;
    }

    Object placeholder = context.options().placeholders().placeholder(placeholderId);

    checkCompatibility(placeholder, "placeholder");
    return placeholder;
  }

  public @Nullable Object defaultValue() {
    DefaultString string = annotation(DefaultString.class);
    if (string != null) {
      return string.value();
    }

    DefaultNumeric numeric = annotation(DefaultNumeric.class);
    if (numeric != null) {
      return numeric.value();
    }

    DefaultDecimal decimal = annotation(DefaultDecimal.class);
    if (decimal != null) {
      return decimal.value();
    }

    DefaultBoolean bool = annotation(DefaultBoolean.class);
    if (bool != null) {
      return bool.value();
    }
    return null;
  }

  public @Nullable Object deserializedDefaultValue() {
    Object deserialized;
    try {
      Object defaultValue = defaultValue();
      if (defaultValue == null) {
        return null;
      }

      deserialized = codec().deserialize(annotate(defaultValue.getClass()), defaultValue, context);
    } catch (IllegalStateException exception) {
      throw new IllegalStateException("Failed to retrieve default value for " + type(), exception);
    }

    checkCompatibility(deserialized, "default value");
    return deserialized;
  }

  public boolean defaultOnFailure() {
    return annotation(DefaultOnFailure.class) != null;
  }

  public Range range() {
    return Range.of(type());
  }

  public boolean isInRange(Object value) {
    return range().isInRange(value);
  }

  private AnnotatedType type() {
    return context.type();
  }

  private <T extends Annotation> T annotation(Class<T> annotationType) {
    return type().getAnnotation(annotationType);
  }

  private TypeCodec<?> codec() {
    return context.codec();
  }

  private void checkCompatibility(Object toCheck, String checkType) {
    Type type = GenericTypeReflector.box(type().getType());

    if (!GenericTypeReflector.isSuperType(type, toCheck.getClass())) {
      throw new IllegalStateException(String.format(
          "Incompatible %s! %s is not compatible with %s",
          checkType, type, toCheck.getClass()
      ));
    }
  }
}
