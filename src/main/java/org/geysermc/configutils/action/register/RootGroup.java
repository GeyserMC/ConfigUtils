package org.geysermc.configutils.action.register;

import java.util.HashSet;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.action.Action;
import org.geysermc.configutils.action.ActionGroup;

public final class RootGroup extends ActionGroup {
  private final Set<Action> rootActions = new HashSet<>();

  RootGroup() {
  }

  @Override
  public @NonNull String groupPrefix() {
    return "";
  }

  @Override
  public @NonNull Action[] children() {
    return rootActions.toArray(new Action[0]);
  }

  public void addChild(Action action) {
    rootActions.add(action);
  }

  public void removeChild(Action action) {
    rootActions.remove(action);
  }
}
