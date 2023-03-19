package org.geysermc.configutils.node.codec.strategy.object;

import java.util.Map;
import org.geysermc.configutils.node.codec.strategy.ResolveStrategy;
import org.geysermc.configutils.node.context.MetaOptions;

public interface ObjectResolveStrategy extends ResolveStrategy<Map<String, MetaOptions>> {
}
