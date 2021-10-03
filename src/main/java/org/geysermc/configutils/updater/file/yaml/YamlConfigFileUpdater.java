package org.geysermc.configutils.updater.file.yaml;

import it.unimi.dsi.fastutil.ints.IntIntImmutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geysermc.configutils.updater.change.Changes;
import org.geysermc.configutils.updater.file.ConfigFileUpdater;
import org.geysermc.configutils.updater.file.ConfigFileUpdaterResult;
import org.geysermc.configutils.util.Utils;

public class YamlConfigFileUpdater implements ConfigFileUpdater {
  public ConfigFileUpdaterResult update(
      Map<String, Object> currentVersion,
      Changes changes,
      Collection<String> ignore,
      Collection<String> copyDirectly,
      List<String> configTemplate) {

    List<String> updated = new ArrayList<>(configTemplate);
    Map<String, Object> newVersion = new HashMap<>(currentVersion);
    Set<String> notFound = new HashSet<>();
    List<String> changed = new ArrayList<>();

    int linesRead = update(
        currentVersion, changes, ignore, copyDirectly, configTemplate,
        0, 0, 0, "", 0, updated, newVersion, notFound, changed
    ).leftInt();

    if (configTemplate.size() != linesRead) {
      return ConfigFileUpdaterResult.failed(new IllegalStateException(String.format(
          "Failed to read the full config! Only read %s lines while config is %s lines long",
          linesRead, configTemplate.size()
      )));
    }
    return ConfigFileUpdaterResult.ok(updated, newVersion, notFound, changed);
  }

  private IntIntPair update(
      Map<String, Object> currentVersion,
      Changes changes,
      Collection<String> ignore,
      Collection<String> copyDirectly,
      List<String> configTemplate,
      int startIndex,
      int indexOffset,
      int spaces,
      String path,
      int parents,
      List<String> updated,
      Map<String, Object> newVersion,
      Set<String> notFound,
      List<String> changed) {

    //todo add support for multi-line

    boolean shouldCopyDirectly = copyDirectly.contains(path);
    boolean hasCopiedDirectly = false;

    String line;
    for (int i = startIndex; i < configTemplate.size(); i++) {
      line = configTemplate.get(i);
      // we don't have to check empty lines or comments
      if (line.isEmpty() || line.charAt(0) == '#') {
        continue;
      }

      StringBuilder lineSpaces = new StringBuilder();
      while (line.charAt(lineSpaces.length()) == ' ') {
        lineSpaces.append(' ');
      }

      // end of subcategory
      if (parents > 0 && spaces >= lineSpaces.length()) {
        return new IntIntImmutablePair(i - startIndex, indexOffset);
      }

      if (shouldCopyDirectly && !hasCopiedDirectly) {
        String correctName = path.substring(0, path.length() - 1);
        String oldName = changes.oldKeyName(correctName);
        Object value = getCurrentVersion(currentVersion, oldName);

        // this is awkward. You want me to copy a non-existing section
        if (value == null) {
          notFound.add(correctName);
          continue;
        }

        if (!(value instanceof Map)) {
          throw new IllegalStateException("I can only copy sections directly! " + correctName);
        }

        indexOffset = writeMap(
            i, indexOffset, lineSpaces.length(), path,
            (Map<String, Object>) value, newVersion, updated
        );
        hasCopiedDirectly = true;
        continue;
      }

      // ignore comments
      if (line.charAt(lineSpaces.length()) == '#') {
        continue;
      }

      String trimmed = line.trim();

      int splitIndex = trimmed.indexOf(':');
      // if the line has a 'key: value' structure
      if (splitIndex != -1) {
        // start of a subcategory
        if (isStartSubcategory(trimmed, splitIndex)) {
          //todo allow subcategory renaming
          String subcategoryName = trimmed.substring(0, splitIndex);
          String newPath = path + subcategoryName + ".";

          IntIntPair result = update(
              currentVersion, changes, ignore, copyDirectly, configTemplate, i + 1, indexOffset,
              lineSpaces.length(), newPath, parents + 1, updated, newVersion, notFound, changed
          );

          i += result.leftInt();
          indexOffset = result.rightInt();
          continue;
        }

        String name = trimmed.substring(0, splitIndex);
        String correctName = path + name;

        // don't change the new value to the current value if it should be ignored.
        // for example 'config-version'
        if (ignore.contains(correctName)) {
          continue;
        }

        String oldName = changes.oldKeyName(correctName);
        Object value = getCurrentVersion(currentVersion, oldName);

        // use default value if the key doesn't exist in the current version
        if (value == null) {
          notFound.add(correctName);
          continue;
        }

        value = changes.newValue(correctName, value);

        changed.add(correctName);
        updated.set(i + indexOffset, lineSpaces + name + ": " + value);
        setNewVersion(newVersion, correctName, value);
      }
    }
    return new IntIntImmutablePair(configTemplate.size(), indexOffset);
  }

  private boolean isStartSubcategory(String trimmed, int splitIndex) {
    // everything after 'key:'
    String substring = trimmed.substring(splitIndex + 1).trim();
    if (substring.isEmpty()) {
      return true;
    }

    for (int i = 0; i < substring.length(); i++) {
      char charAt = substring.charAt(i);
      if (charAt == '#') {
        return true;
      } else if (charAt != ' ') {
        return false;
      }
    }
    return true;
  }

  private Object getCurrentVersion(Map<String, Object> currentVersion, String correctName) {
    Map<String, Object> curSubcategory = currentVersion;
    String[] parts = correctName.split("\\.");
    for (int i = 0; i < parts.length - 1; i++) {
      curSubcategory = (Map<String, Object>) curSubcategory.get(parts[i]);
      // can be null if the category doesn't exist in the current version
      if (curSubcategory == null) {
        return null;
      }
    }
    return curSubcategory.get(parts[parts.length - 1]);
  }

  public void setNewVersion(Map<String, Object> newVersion, String correctName, Object value) {
    Map<String, Object> curSubcategory = newVersion;
    String[] parts = correctName.split("\\.");
    for (int i = 0; i < parts.length - 1; i++) {
      Map<String, Object> newSubcategory = (Map<String, Object>) curSubcategory.get(parts[i]);
      // can be null if the category doesn't exist in the current version
      if (newSubcategory == null) {
        newSubcategory = new HashMap<>();
        curSubcategory.put(parts[i], newSubcategory);
        curSubcategory = newSubcategory;
      }
    }
    curSubcategory.put(parts[parts.length - 1], value);
  }

  private int writeMap(
      int index, int indexOffset, int spaces, String path, Map<String, Object> map,
      Map<String, Object> newVersion, List<String> updated) {
    //todo don't strip the comments

    int entriesHandled = 0;
    String prefix = Utils.repeat(' ', spaces);

    for (Map.Entry<String, Object> entry : map.entrySet()) {
      if (entry.getValue() instanceof Map) {
        // subcategories have to be defined as well
        updated.add(index + indexOffset, prefix + entry.getKey() + ':');
        indexOffset++;

        indexOffset = writeMap(
            index + entriesHandled, indexOffset, spaces + 2, path + entry.getKey() + ".",
            (Map<String, Object>) entry.getValue(), newVersion, updated
        );
        continue;
      }

      //todo add Set/List support
      updated.add(index + indexOffset, prefix + entry.getKey() + ": " + entry.getValue());
      setNewVersion(newVersion, path + entry.getKey(), entry.getValue());
      indexOffset++;
    }

    return indexOffset;
  }
}
