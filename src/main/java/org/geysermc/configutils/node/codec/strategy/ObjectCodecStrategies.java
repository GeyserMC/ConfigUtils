package org.geysermc.configutils.node.codec.strategy;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.node.codec.strategy.ObjectCodecStrategies.ObjectCodecStrategy.FinaliseObjectState;
import org.geysermc.configutils.node.codec.strategy.ObjectCodecStrategies.ObjectCodecStrategy.ReadObjectTreeState;
import org.geysermc.configutils.node.codec.strategy.StandardStrategyStates.InitialState;

public final class ObjectCodecStrategies {
  public static CodecStrategy proxiedObjectStrategy(AnnotatedType type) {
    return new ProxiedObjectCodecStrategy();
  }

  static abstract class ObjectCodecStrategy
      <R extends ReadObjectTreeState, F extends FinaliseObjectState>
      implements CodecStrategy {
    private final Class<R> readObjectTreeStateType;
    private final Class<F> finaliseObjectStateType;

    protected ObjectCodecStrategy(
        @NonNull Class<R> readObjectTreeStateType,
        @NonNull Class<F> finaliseObjectStateType
    ) {
      this.readObjectTreeStateType = Objects.requireNonNull(readObjectTreeStateType);
      this.finaliseObjectStateType = Objects.requireNonNull(finaliseObjectStateType);
    }

    public abstract Object readObjectTree(InitialState data);
    public abstract Object finaliseObject(R data);

    @Override
    @SuppressWarnings("unchecked")
    public Object handleState(StrategyState state) {
      if (state instanceof InitialState) {
        return readObjectTree((InitialState) state);
      }
      if (readObjectTreeStateType.isInstance(state)) {
        return finaliseObject((R) state);
      }
      if (finaliseObjectStateType.isInstance(state)) {
        return StandardStrategyStates.finishedState(((F) state).result());
      }
      throw new IllegalStateException(String.format(
          "The given state %s type is not valid for %s",
          state.getClass().getSimpleName(), getClass().getSimpleName()
      ));
    }

    protected interface ReadObjectTreeState extends StrategyState {
    }

    protected interface FinaliseObjectState extends StrategyState {
      Object result();
    }
  }

  public static final class ProxiedObjectCodecStrategy extends ObjectCodecStrategy {


    public ProxiedObjectCodecStrategy() {
      super(ProxiedReadObjectTreeState.class, ProxiedFinaliseObjectState.class);
    }

    @Override
    public Object readObjectTree() {
      return null;
    }

    @Override
    public Object finaliseObject() {
      return null;
    }

    protected static final class ProxiedReadObjectTreeState implements ReadObjectTreeState {
      private final Method[] cachedMethods;

      private ProxiedReadObjectTreeState(Method[] cachedMethods) {
        this.cachedMethods = cachedMethods;
      }
    }

    protected static final class ProxiedFinaliseObjectState implements FinaliseObjectState {
      private final Object result;

      public ProxiedFinaliseObjectState(Object result) {
        this.result = result;
      }

      @Override
      public Object result() {
        return result;
      }
    }
  }
}
