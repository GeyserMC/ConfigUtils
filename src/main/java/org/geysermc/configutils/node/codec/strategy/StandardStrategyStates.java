package org.geysermc.configutils.node.codec.strategy;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.node.context.NodeContext;

public class StandardStrategyStates {
  public static InitialState initialState(AnnotatedType type, Object value, NodeContext context) {
    return new InitialState(type, value, context);
  }

  public static <T> FinishedState<T> finishedState(T result) {
    return new FinishedState<>(result);
  }

  public static final class InitialState implements StrategyState {
    private final AnnotatedType type;
    private final Object rawValue;
    private final NodeContext context;

    private InitialState(
        @NonNull AnnotatedType type,
        @NonNull Object rawValue,
        @NonNull NodeContext context
    ) {
      this.type = Objects.requireNonNull(type);
      this.rawValue = Objects.requireNonNull(rawValue);
      this.context = Objects.requireNonNull(context);
    }

    public @NonNull AnnotatedType type() {
      return type;
    }

    public @NonNull Object rawValue() {
      return rawValue;
    }

    public @NonNull NodeContext context() {
      return context;
    }
  }

  public static final class FinishedState<T> implements StrategyState {
    private final T result;

    private FinishedState(T result) {
      this.result = result;
    }

    public T result() {
      return result;
    }
  }
}
