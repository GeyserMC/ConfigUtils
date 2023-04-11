package org.geysermc.configutils.node.context.option;

import io.leangen.geantyref.GenericTypeReflector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.node.codec.type.TypeCodec;
import org.geysermc.configutils.node.context.NodeContext;
import org.geysermc.configutils.node.meta.Comment;
import org.geysermc.configutils.node.meta.ConfigSection;
import org.geysermc.configutils.node.meta.ConfigVersion;
import org.geysermc.configutils.node.meta.DefaultOnFailure;
import org.geysermc.configutils.node.meta.Defaults.DefaultBoolean;
import org.geysermc.configutils.node.meta.Defaults.DefaultDecimal;
import org.geysermc.configutils.node.meta.Defaults.DefaultNumeric;
import org.geysermc.configutils.node.meta.Defaults.DefaultString;
import org.geysermc.configutils.node.meta.Exclude;
import org.geysermc.configutils.node.meta.Hidden;
import org.geysermc.configutils.node.meta.Placeholder;
import org.geysermc.configutils.node.meta.Range;
import org.geysermc.configutils.util.ReflectionUtils;

public class MetaOptions {
  private final NodeContext context;

  public MetaOptions(NodeContext context) {
    this.context = context;
  }

  public Object applyMeta(Object value) {
    boolean wasNull = value == null;

    if (value == null) {
      value = deserializedDefaultValue();
    }

    if (value == null) {
      value = resolvedPlaceholder();
    }

    if (value == null) {
      throw new IllegalStateException(String.format(
          "Node %s has neither a value nor a default value",
          context.fullKey()
      ));
    }

    if (wasNull) {
      context.markChanged();
    }

    if (!isInRange(value)) {
      Range range = range();
      //todo DefaultOnFailure & add error handler option
      throw new IndexOutOfBoundsException(String.format(
          "'%s' (fullKey: %s) is not in the allowed range of from: %s, to: %s!",
          value, context.fullKey(), range.from(), range.to()
      ));
    }

    return value;
  }

  public @Nullable String comment() {
    Comment comment = annotation(Comment.class);
    return comment != null ? comment.value() : null;
  }

  public boolean isSection() {
    return annotation(ConfigSection.class) != null || annotation(ConfigVersion.class) != null;
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

    if (placeholder == null) {
      throw new IllegalStateException(String.format(
          "Node %s is annotated with Placeholder but no placeholder with id %s was provided",
          context.fullKey(), placeholderId
      ));
    }

    return deserializeAndCheck(placeholder, "placeholder");
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
    try {
      Object defaultValue = defaultValue();
      if (defaultValue == null) {
        return null;
      }

      return deserializeAndCheck(defaultValue, "default value");
    } catch (IllegalStateException exception) {
      throw new IllegalStateException("Failed to retrieve default value for " + type(), exception);
    }
  }

  public boolean defaultOnFailure() {
    return annotation(DefaultOnFailure.class) != null;
  }

  public boolean isDefaultOrPlaceholder(Object value) {
    Object defaultValue = defaultValue();
    return defaultValue != null && defaultValue.equals(value) || placeholder() != null;
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
    return ReflectionUtils.findAnnotation(annotationType, type());
  }

  private TypeCodec<?> codec() {
    return context.codec();
  }

  private Object deserializeAndCheck(Object toHandle, String checkType) {
    Object deserialized = codec().deserialize(type(), toHandle, context);

    Type type = GenericTypeReflector.box(type().getType());
    if (!GenericTypeReflector.isSuperType(type, deserialized.getClass())) {
      throw new IllegalStateException(String.format(
          "Incompatible %s! %s is not compatible with %s",
          checkType, type, deserialized.getClass()
      ));
    }

    return deserialized;
  }

  public boolean isExcluded() {
    return annotation(Exclude.class) != null;
  }

  public boolean isHidden() {
    return annotation(Hidden.class) != null;
  }
}
