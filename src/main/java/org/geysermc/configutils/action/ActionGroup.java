package org.geysermc.configutils.action;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.action.register.RootGroup;
import org.geysermc.configutils.util.Pair;

public abstract class ActionGroup implements Action {
  @NonNull
  public abstract String groupPrefix();

  @NonNull
  public abstract Action[] children();

  @Nullable
  public final Pair<String, Action> child(@NonNull String line) {
    Objects.requireNonNull(line);
    if (line.isEmpty()) {
      throwIfNotRoot(line);
      return null;
    }

    children: for (Action child : children()) {

      if (child instanceof SingleAction) {
        if (((SingleAction) child).actionPrefix() == line.charAt(0)) {
          return new Pair<>(line.substring(1), child);
        }
        continue;
      }

      if (child instanceof ActionGroup) {
        String prefix = ((ActionGroup) child).groupPrefix();
        if (line.length() < prefix.length()) {
          continue;
        }

        for (int i = 0; i < prefix.length(); i++) {
          if (line.charAt(i) != prefix.charAt(i)) {
            continue children;
          }
        }

        return ((ActionGroup) child).child(line.substring(prefix.length()));
      }

      throw new IllegalStateException("Child has to be an instance of SingleAction or ActionGroup");
    }

    throwIfNotRoot(line);
    return null;
  }

  private void throwIfNotRoot(String line) {
    // RootGroup also extends ActionGroup, however, not every line in RootGroup has to be an action.
    if (!(this instanceof RootGroup)) {
      throw new IllegalStateException(String.format(
          "Cannot find an action that matches '%s' in %s",
          line,
          getClass().getSimpleName()
      ));
    }
  }
}
