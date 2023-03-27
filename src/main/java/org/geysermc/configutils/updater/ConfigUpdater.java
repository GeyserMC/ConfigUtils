package org.geysermc.configutils.updater;

import static io.leangen.geantyref.GenericTypeReflector.annotate;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geysermc.configutils.node.codec.strategy.object.ObjectResolveStrategy;
import org.geysermc.configutils.node.codec.type.IntegerCodec;
import org.geysermc.configutils.node.codec.type.ObjectCodec;
import org.geysermc.configutils.node.context.NodeContext;
import org.geysermc.configutils.updater.change.Changes;
import org.geysermc.configutils.updater.file.ConfigFileUpdaterResult;

public class ConfigUpdater {
  public ConfigFileUpdaterResult update(
      NodeContext context,
      Map<String, ?> currentConfig,
      String configVersionName,
      Changes changes,
      Collection<String> copyDirectly
  ) {
    try {
      return update0(context, currentConfig, configVersionName, changes, copyDirectly);
    } catch (Throwable throwable) {
      return ConfigFileUpdaterResult.failed(throwable);
    }
  }

  private ConfigFileUpdaterResult update0(
      NodeContext context,
      Map<String, ?> currentConfig,
      String configVersionName,
      Changes changes,
      Collection<String> copyDirectly
  ) {

    Integer version = null;
    if (currentConfig.containsKey(configVersionName)) {
      Object versionObject = currentConfig.get(configVersionName);
      try {
        version = IntegerCodec.INSTANCE.deserialize(
            annotate(versionObject.getClass()), versionObject, null
        );
      } catch (NumberFormatException exception) {
        throw new IllegalStateException(
            String.format("Got invalid config version: %s", versionObject),
            exception
        );
      }
    }

    if (version == null) {
      version = 0;
    }

    if (version < 0) {
      throw new IllegalStateException(String.format("Got a negative config version: %s", version));
    }

    if (version > context.configVersion()) {
      throw new IllegalStateException(
          "Cannot update a configuration that is newer than the latest available config version"
      );
    }

    if (version == context.configVersion()) {
      return ConfigFileUpdaterResult.upToDate();
    }

    // don't need to update / handle the config version in general
    currentConfig.remove(configVersionName);

    // let's update the config
    changes.select(version, context.configVersion());

    ObjectResolveStrategy resolveStrategy =
        ((ObjectCodec) context.codecs().get(Object.class)).resolveStrategy();

    Set<String> notFound = new HashSet<>();
    Map<String, Object> result = update(
        resolveStrategy,
        resolveStrategy.resolve(context.type(), context),
        currentConfig,
        changes,
        copyDirectly,
        notFound
    );

    // re-add the config version
    result.put(configVersionName, context.configVersion());

    return ConfigFileUpdaterResult.ok(result, notFound);
  }

  private Map<String, Object> update(
      ObjectResolveStrategy resolveStrategy,
      List<NodeContext> latestConfig,
      Map<String, ?> currentConfig,
      Changes changes,
      Collection<String> copyDirectly,
      Set<String> notFound
  ) {
    Map<String, Object> converted = new HashMap<>();
    for (NodeContext node : latestConfig) {
      String fullKey = node.fullKey();

      String oldKey = changes.oldKeyName(fullKey);
      String oldEncodedKey = node.options().codec().nameEncoder().apply(oldKey);
      Object currentValue = currentValue(currentConfig, oldEncodedKey);

      if (currentValue == null) {
        notFound.add(fullKey);
        continue;
      }

      if (copyDirectly.contains(node.fullKey())) {
        converted.put(node.key(), currentValue);
        continue;
      }

      Object newValue;
      if (currentValue instanceof Map) {
        newValue = update(
            resolveStrategy,
            resolveStrategy.resolve(node.type(), node),
            currentConfig,
            changes,
            copyDirectly,
            notFound
        );
      } else {
        newValue = changes.newValue(fullKey, currentValue);
      }
      converted.put(node.key(), newValue);
    }
    return converted;
  }

  @SuppressWarnings("unchecked")
  private Object currentValue(Map<String, ?> currentConfig, String fullKey) {
    Map<String, ?> curSubcategory = currentConfig;
    String[] parts = fullKey.split("\\.");
    for (int i = 0; i < parts.length - 1; i++) {
      curSubcategory = (Map<String, ?>) curSubcategory.get(parts[i]);
      // can be null if the category doesn't exist in the current version
      if (curSubcategory == null) {
        return null;
      }
    }
    return curSubcategory.get(parts[parts.length - 1]);
  }
}
