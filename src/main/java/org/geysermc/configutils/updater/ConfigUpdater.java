package org.geysermc.configutils.updater;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.geysermc.configutils.parser.template.TemplateParseResult;
import org.geysermc.configutils.updater.change.Changes;
import org.geysermc.configutils.updater.file.ConfigFileUpdaterResult;
import org.geysermc.configutils.updater.file.yaml.YamlConfigFileUpdater;
import org.geysermc.configutils.util.Utils;
import org.yaml.snakeyaml.Yaml;

public class ConfigUpdater {
  public ConfigFileUpdaterResult update(
      List<String> currentConfig,
      String configVersionName,
      TemplateParseResult parseResult,
      Changes changes,
      Collection<String> copyDirectly,
      String... ignore) {

    String versionString = null;
    for (String configLine : currentConfig) {
      if (configLine.startsWith(configVersionName + ':')) {
        versionString = configLine.substring(configLine.indexOf(':') + 1).trim();
        break;
      }
    }

    Integer version = null;
    if (versionString != null) {
      try {
        version = Integer.parseInt(versionString);
      } catch (NumberFormatException e) {
        return ConfigFileUpdaterResult.failed(new IllegalStateException(String.format(
            "Got invalid config version: %s", versionString
        )));
      }
    }

    if (version == null) {
      version = 0;
    }

    if (version < 0) {
      return ConfigFileUpdaterResult.failed(new IllegalStateException(String.format(
          "Got a negative config version: %s", version
      )));
    }

    if (!parseResult.succeeded()) {
      return ConfigFileUpdaterResult.failed(new IllegalStateException(
          "Couldn't update the config because we couldn't read the newest config template",
          parseResult.error()
      ));
    }

    // search for the config version key
    int latestVersion = 0;
    for (String configLine : parseResult.templateLines()) {
      if (configLine.startsWith(configVersionName + ':')) {
        latestVersion = Integer.parseInt(configLine.substring(configLine.indexOf(':') + 1).trim());
        break;
      }
    }

    if (version > latestVersion) {
      return ConfigFileUpdaterResult.failed(new IllegalStateException(
          "Cannot update a configuration that is newer than the latest available config version"
      ));
    }

    StringBuilder currentConfigBuilder = new StringBuilder();
    for (String line : currentConfig) {
      currentConfigBuilder.append(line).append(System.lineSeparator());
    }
    Map<String, Object> currentConfigMap = new Yaml().load(currentConfigBuilder.toString());

    if (version == latestVersion) {
      return ConfigFileUpdaterResult.ok(
          currentConfig, currentConfigMap, Collections.emptySet(), Collections.emptyList()
      );
    }

    // let's update the config
    changes.select(version, latestVersion);

    return new YamlConfigFileUpdater().update(
        currentConfigMap, changes, Utils.merge(ignore, configVersionName),
        copyDirectly, parseResult.templateLines()
    );
  }
}
