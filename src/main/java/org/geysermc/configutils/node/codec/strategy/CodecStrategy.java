package org.geysermc.configutils.node.codec.strategy;

public interface CodecStrategy {
  StrategyState handleState(StrategyState state);
}
