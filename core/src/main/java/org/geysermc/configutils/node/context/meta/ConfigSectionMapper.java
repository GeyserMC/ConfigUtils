package org.geysermc.configutils.node.context.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geysermc.configutils.node.meta.ConfigSection;

public class ConfigSectionMapper
    implements MetaCacheMapper<ConfigSection, Map<String, List<String>>> {
  public static final MetaCacheMapper<ConfigSection, Map<String, List<String>>> MAPPER =
      new ConfigSectionMapper();

  @Override
  public Map<String, List<String>> map(List<String> lines) {
    Map<String, List<String>> classMethodOrder = new HashMap<>();

    String className = null;
    List<String> methodOrder = null;
    for (String line : lines) {
      String[] sections = line.split(":", 2);
      if (sections.length != 2) {
        throw new IllegalStateException(
            "Invalid ConfigSection meta line! Please rebuild the application"
        );
      }

      switch (sections[0]) {
        case "c":
          if (className != null) {
            classMethodOrder.put(className, Collections.unmodifiableList(methodOrder));
          }
          className = sections[1];
          methodOrder = new ArrayList<>();
          break;
        case "m":
          if (methodOrder == null) {
            throw new IllegalStateException(
                "Invalid ConfigSection meta! Please rebuild the application"
            );
          }
          methodOrder.add(sections[1]);
          break;
        default:
          throw new IllegalStateException(
              "Unrecognised ConfigSection meta identifier! Is ConfigUtils up to date?"
          );
      }
    }
    // push last item
    if (className != null) {
      classMethodOrder.put(className, Collections.unmodifiableList(methodOrder));
    }

    return Collections.unmodifiableMap(classMethodOrder);
  }

  @Override
  public Class<ConfigSection> annotationClass() {
    return ConfigSection.class;
  }
}
