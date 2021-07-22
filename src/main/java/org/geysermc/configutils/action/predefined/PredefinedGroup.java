package org.geysermc.configutils.action.predefined;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.configutils.action.Action;
import org.geysermc.configutils.action.ActionGroup;

public class PredefinedGroup extends ActionGroup {
  @Override
  @NonNull
  public String groupPrefix() {
    return ">>";
  }

  @Override
  public @NonNull Action[] children() {
    return new Action[] {
      new DefineImportAction(), new ImportSectionAction(), new WriteRemainingAction()
    };
  }
}
