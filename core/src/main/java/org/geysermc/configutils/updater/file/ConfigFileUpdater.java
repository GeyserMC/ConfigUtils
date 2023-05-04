package org.geysermc.configutils.updater.file;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.geysermc.configutils.updater.change.Changes;

public interface ConfigFileUpdater {
  ConfigFileUpdaterResult update(
      Map<String, Object> currentVersion,
      Changes changes,
      Collection<String> ignore,
      Collection<String> copyDirectly,
      List<String> configTemplate);
}
