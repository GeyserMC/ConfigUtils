package org.geysermc.configutils.node;

import java.lang.reflect.AnnotatedType;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.node.codec.strategy.CodecStrategy;
import org.geysermc.configutils.node.codec.strategy.StandardStrategyStates;
import org.geysermc.configutils.node.codec.strategy.StandardStrategyStates.FinishedState;
import org.geysermc.configutils.node.codec.strategy.StrategyState;

public final class Node<T> {
  private final String key;
  private final AnnotatedType type;
  private final CodecStrategy strategy;
  private StrategyState strategyState;

  private Node(
      @Nullable String key,
      @NonNull AnnotatedType type,
      @Nullable CodecStrategy strategy,
      @NonNull StrategyState strategyState
  ) {
    this.key = key;
    this.type = Objects.requireNonNull(type);
    this.strategy = strategy;
    this.strategyState = Objects.requireNonNull(strategyState);
  }

  public static <T> Node<T> of(
      @Nullable String key,
      @NonNull AnnotatedType type,
      @Nullable T value
  ) {
    return of(key, type, null, value);
  }

  public static <T> Node<T> of(
      @Nullable String key,
      @NonNull AnnotatedType type,
      @Nullable CodecStrategy strategy,
      @Nullable T value
  ) {
    return new Node<>(key, type, strategy, StandardStrategyStates.finishedState(value));
  }

  public @Nullable String key() {
    return key;
  }

  public @NonNull AnnotatedType type() {
    return type;
  }

  public @Nullable CodecStrategy strategy() {
    return strategy;
  }

  public @NonNull StrategyState strategyState() {
    return strategyState;
  }

  public Node<T> strategyState(@NonNull StrategyState strategyState) {
    this.strategyState = Objects.requireNonNull(strategyState);
    return this;
  }

  @SuppressWarnings("unchecked")
  public T value() {
    if (!(strategyState instanceof FinishedState)) {
      throw new IllegalStateException("The node value hasn't yet finished processing");
    }
    return ((FinishedState<T>) strategyState).result();
  }

  public void nextState() {
    //todo how do we deny absents?
    if (strategy != null) {
      strategyState(strategy.handleState(strategyState()));
    }
  }

  public Node<T> withKey(@Nullable String key) {
    return new Node<>(key, type, strategy, strategyState);
  }
}
