package org.geysermc.configutils.node.codec.type;

import io.leangen.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import org.geysermc.configutils.node.context.NodeContext;

public abstract class TypeCodec<T> {
  private final TypeToken<T> type;

  TypeCodec(TypeToken<T> type) {
    this.type = type;
  }

  TypeCodec(Class<T> type) {
    if (type.getTypeParameters().length > 0) {
      throw new IllegalStateException("Provide a TypeToken when the type has type parameters");
    }
    this.type = TypeToken.get(type);
  }

  public abstract T deserialize(AnnotatedType type, Object value, NodeContext context);

  public abstract Object serialize(AnnotatedType type, T value, NodeContext context);

  public TypeToken<T> type() {
    return type;
  }
}
