package org.geysermc.configutils.node.codec.strategy.object;

import java.util.Map;
import org.geysermc.configutils.node.codec.strategy.ResolveStrategy;
import org.geysermc.configutils.node.context.NodeContext;

public interface ObjectResolveStrategy extends ResolveStrategy<Map<String, NodeContext>> {
}
