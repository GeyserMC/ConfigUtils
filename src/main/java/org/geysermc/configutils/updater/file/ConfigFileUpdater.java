package org.geysermc.configutils.updater.file;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ConfigFileUpdater {
  ConfigFileUpdaterResult update(
      Map<String, Object> currentVersion,
      Map<String, String> renames,
      Collection<String> ignore,
      Collection<String> copyDirectly,
      List<String> configTemplate);
}
