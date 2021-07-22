package org.geysermc.configutils.action.register;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.configutils.action.Action;
import org.geysermc.configutils.action.ActionGroup;
import org.geysermc.configutils.action.SingleAction;
import org.geysermc.configutils.util.Pair;

public final class RegisteredActions {
  private final RootGroup rootActions = new RootGroup();

  public RegisteredActions registerAction(@NonNull Action action) {
    Objects.requireNonNull(action);
    boolean single = action instanceof SingleAction;
    boolean group = action instanceof ActionGroup;

    if (single != group) {
      rootActions.addChild(action);
      return this;
    }
    throw new IllegalStateException("Cannot register an invalid action");
  }

  public RegisteredActions removeAction(@NonNull Action action) {
    rootActions.removeChild(Objects.requireNonNull(action));
    return this;
  }

  @Nullable
  public Pair<String, Action> getActionFromLine(@NonNull String line) {
    return rootActions.child(line);
  }
}